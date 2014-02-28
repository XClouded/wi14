package sapphire.policy.durable;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import sapphire.policy.SapphirePolicy;

public class ExplicitCheckpointPolicy extends SapphirePolicy {
	
	public static class ExplicitCheckpointClientPolicy extends SapphireClientPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private ExplicitCheckpointServerPolicy server;
		private ExplicitCheckpointGroupPolicy group;
		
		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (ExplicitCheckpointGroupPolicy) group;
		}
		
		@Override
		public void setServer(SapphireServerPolicy server) {
			this.server = (ExplicitCheckpointServerPolicy) server;
		}
		
		@Override
		public SapphireServerPolicy getServer() {
			return server;
		}
		
		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}

		public void checkpoint() {
			server.checkpoint();
		}
	}
	
	public static class ExplicitCheckpointServerPolicy extends SapphireServerPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int MAX_RPCS = 1000;
		
		private ExplicitCheckpointGroupPolicy group;
		private String storageKey;
		private Semaphore rpcCounter;
		
		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (ExplicitCheckpointGroupPolicy) group;
			rpcCounter = new Semaphore(MAX_RPCS, true);
			storageKey = sapphire_durable_put(appObject.getObject());
			this.group.setStorageKey(storageKey);
		}
		
		@Override
		public Object onRPC(String method, Object[] params) throws Exception {
			rpcCounter.acquire();
			Object ret =  super.onRPC(method, params);
			rpcCounter.release();
			
			return ret;
		}
		
		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}
		
		@Override
		public void onMembershipChange() { }
		
		public void checkpoint() {
			rpcCounter.acquireUninterruptibly(MAX_RPCS - 1);
			if (!sapphire_durable_put(appObject.getObject()).equals(storageKey)) {
				throw new IllegalStateException("Storage keys do not match");
			}
			rpcCounter.release(MAX_RPCS);
		}
	}

	public static class ExplicitCheckpointGroupPolicy extends SapphireGroupPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private String storageKey;
		
		@Override
		public void onCreate(SapphireServerPolicy server) {
			
		}
		
		@Override
		public void addServer(SapphireServerPolicy server) {
			
		}
		
		@Override
		public ArrayList<SapphireServerPolicy> getServers() {
			return null;
		}
		
		@Override
		public void onFailure(SapphireServerPolicy server) {
			
		}
		
		public String getStorageKey() {
			return storageKey;
		}
		
		public void setStorageKey(String storageKey) {
			this.storageKey = storageKey;
		}
	}

}
