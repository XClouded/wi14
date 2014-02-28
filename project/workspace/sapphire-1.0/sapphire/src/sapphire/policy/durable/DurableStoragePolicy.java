package sapphire.policy.durable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
//import java.util.logging.Logger;

import sapphire.policy.SapphirePolicy;

public class DurableStoragePolicy extends SapphirePolicy {
	
	public static class DurableStorageClientPolicy extends SapphireClientPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private DurableStorageServerPolicy server;
		private DurableStorageGroupPolicy group;
		
		@Override
		public void setServer(SapphireServerPolicy server) {
			this.server = (DurableStorageServerPolicy) server;
		}

		@Override
		public SapphireServerPolicy getServer() {
			return server;
		}

		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}

		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (DurableStorageGroupPolicy) group;
		}
	}

	public static class DurableStorageServerPolicy extends SapphireServerPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int RPCS_PER_STORE = 3;
		private DurableStorageGroupPolicy group;
		private int numRPCsSinceLastStore;
		private int numActiveRPCs;
		private String storageKey;

		private final Lock lock = new ReentrantLock();
		private final Condition objFree = lock.newCondition();
		private final Condition objStored = lock.newCondition();
		
		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (DurableStorageGroupPolicy) group;
			numRPCsSinceLastStore = 0;
			numActiveRPCs = 0;
			this.storageKey = sapphire_durable_put(appObject.getObject());
		}
		
		@Override
		public Object onRPC(String method, Object[] params) throws Exception {
			lock.lock();
			try {
				// Wait for "storer" to finish, if one exists
				while (numRPCsSinceLastStore > RPCS_PER_STORE) {
					objStored.await();
				}
				if (numRPCsSinceLastStore == RPCS_PER_STORE) {
					// Wait for all other RPCs to finish before storing object
					while (numActiveRPCs > 0) {
						objFree.await();
					}
					sapphire_durable_put(appObject.getObject());
					numRPCsSinceLastStore = 0;
					objStored.signalAll();
				}
				numActiveRPCs++;
				numRPCsSinceLastStore++;
			} finally {
				lock.unlock();
			}
			Object retObj = super.onRPC(method, params);
			
			// Adjust counters
			lock.lock();
			numActiveRPCs--;
			objFree.signal();
			lock.unlock();
			
			return retObj;
		}
		
		@Override
		public void onMembershipChange() {}
		
		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}
		
		public String getStorageKey() {
			return storageKey;
		}
	}
	
	public static class DurableStorageGroupPolicy extends SapphireGroupPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String storageKey;

		@Override
		public void addServer(SapphireServerPolicy server) {}

		@Override
		public void onFailure(SapphireServerPolicy server) {
			server.sapphire_getAppObject().setObject((Serializable) server.sapphire_durable_get(storageKey));
		}

		@Override
		public SapphireServerPolicy onRefRequest() {
			return null;
		}

		@Override
		public ArrayList<SapphireServerPolicy> getServers() {
			return null;
		}

		@Override
		public void onCreate(SapphireServerPolicy server) {
			storageKey = ((DurableStorageServerPolicy) server).getStorageKey();
		}
		
	}
}
