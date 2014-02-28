package sapphire.policy.immutable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import sapphire.common.AppObject;
import sapphire.kernel.common.KernelOID;
import sapphire.policy.SapphirePolicy;
import sapphire.policy.SapphirePolicy.SapphireClientPolicy;
import sapphire.policy.SapphirePolicy.SapphireGroupPolicy;
import sapphire.policy.SapphirePolicy.SapphireServerPolicy;

public class ImmutablePolicy extends SapphirePolicy {
	
	public static class ImmutableClientPolicy extends SapphireClientPolicy {
		public ImmutableServerPolicy server;
		public ImmutableGroupPolicy group;
		//public AppObject copy;
		private static ConcurrentHashMap<KernelOID, AppObject> store = new ConcurrentHashMap<KernelOID,AppObject>();
		private KernelOID oid;
		
		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (ImmutableGroupPolicy)group;
		}

		@Override
		public void setServer(SapphireServerPolicy server) {
			this.server = (ImmutableServerPolicy)server;
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
		public Object onRPC(String method, Object[] params) throws Exception{
			if(oid == null) {
				oid = server.getOID();
			}
			AppObject copy = store.get(oid);
			if(copy == null) {
				copy = server.getCopy();
				store.put(oid, copy);
			}
			return copy.invoke(method, params);
		}
	
	}
	
	public static class ImmutableServerPolicy extends SapphireServerPolicy {
		public ImmutableGroupPolicy group;
		
		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (ImmutableGroupPolicy)group;
		}

		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}

		@Override
		public void onMembershipChange() {
		}
		
		public AppObject getCopy() throws Exception {
			return sapphire_getAppObject();
		}
		
		public KernelOID getOID() {
			return this.oid;
		}
	}
	
	public static class ImmutableGroupPolicy extends SapphireGroupPolicy {
		//the servers replicating this object, the zeroth object is the master
		ArrayList<ImmutableServerPolicy> servers = new ArrayList<ImmutableServerPolicy>();
		
		@Override
		public void onCreate(SapphireServerPolicy server) {
			servers.add((ImmutableServerPolicy)server);
		}

		@Override
		public void addServer(SapphireServerPolicy server) {
			servers.add((ImmutableServerPolicy)server);
		}

		@Override
		public ArrayList<SapphireServerPolicy> getServers() {
			return new ArrayList<SapphireServerPolicy>(servers);
		}

		@Override
		public void onFailure(SapphireServerPolicy server) {
			
		}
		
	}
}
