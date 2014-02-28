package sapphire.policy.scalability;

import java.rmi.RemoteException;
import java.util.ArrayList;

import sapphire.policy.SapphirePolicy;

public class SimpleScalabilityPolicy extends SapphirePolicy {

	public static class SimpleScalabilityClientPolicy extends SapphireClientPolicy {
		public SimpleScalabilityServerPolicy server;
		public SimpleScalabilityGroupPolicy group;
		transient private boolean assigned = false;

		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (SimpleScalabilityGroupPolicy) group;
		}

		@Override
		public void setServer(SapphireServerPolicy server) {
			this.server = (SimpleScalabilityServerPolicy)server;
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
		public Object onRPC(String method, Object[] params) throws Exception {
			if (!assigned) {
				setServer(group.onRefRequest());
				assigned = true;
			}
			return server.onRPC(method, params);
		}
	}

	public static class SimpleScalabilityServerPolicy extends SapphireServerPolicy {
		public SimpleScalabilityGroupPolicy group;

		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (SimpleScalabilityGroupPolicy)group;
		}

		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}

		@Override
		public void onMembershipChange() {
		}

		public SimpleScalabilityServerPolicy scalabilityReplicate() {
			return (SimpleScalabilityServerPolicy) sapphire_replicate();
		}

		public void scalabilityPin(String region) throws RemoteException {
			sapphire_pin(region);
		}
	}

	public static class SimpleScalabilityGroupPolicy extends SapphireGroupPolicy {
		ArrayList<SimpleScalabilityServerPolicy> servers = new ArrayList<SimpleScalabilityServerPolicy>();
		private String lock = new String("lock");
		private int no_servers = 4;
		private int crt = 0;

		@Override
		public void onCreate(SapphireServerPolicy server) {
			SimpleScalabilityServerPolicy firstServer = (SimpleScalabilityServerPolicy) server;
			servers.add(firstServer);
			try {
				ArrayList<String> regions = sapphire_getRegions();
				firstServer.scalabilityPin(regions.get(0));
				for (int i = 1; (i < no_servers) && (i < regions.size()); i++) {
					SimpleScalabilityServerPolicy replica = firstServer.scalabilityReplicate();
					replica.scalabilityPin(regions.get(i));
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				throw new Error("Could not create new Scalability group policy.");
			}
		}

		@Override
		public void addServer(SapphireServerPolicy server) {
			servers.add((SimpleScalabilityServerPolicy)server);
		}

		@Override
		public ArrayList<SapphireServerPolicy> getServers() {
			return new ArrayList<SapphireServerPolicy>(servers);
		}

		@Override
		public void onFailure(SapphireServerPolicy server) {
		}

		@Override
		public SapphireServerPolicy onRefRequest() {
			synchronized (lock) {
				return servers.get(crt++%no_servers);
			}
		}
	}
}