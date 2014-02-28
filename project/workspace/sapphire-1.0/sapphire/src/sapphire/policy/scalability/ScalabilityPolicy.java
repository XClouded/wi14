package sapphire.policy.scalability;

import java.rmi.RemoteException;
import java.util.ArrayList;

import sapphire.policy.SapphirePolicy;

public class ScalabilityPolicy extends SapphirePolicy {

	public static class ScalabilityClientPolicy extends SapphireClientPolicy {
		public ScalabilityServerPolicy server;
		public ScalabilityGroupPolicy group;

		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (ScalabilityGroupPolicy)group;
		}

		@Override
		public void setServer(SapphireServerPolicy server) {
			this.server = (ScalabilityServerPolicy)server;
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
			// Keep trying until we have an un-overloaded server
			while (true) {
				try {
					return super.onRPC(method, params);
				} catch (ObjectOverloadedException e) {
					//System.out.println("Object overloaded... Retrying");
					//System.out.println("Previous server = " + server);
					server = (ScalabilityServerPolicy) group.onRefRequest();
					//System.out.println("New server = " + server);
				}
			}
		}

		public double getMaxLoad() {
			return server.getMaxLoad();
		}
	}

	public static class ScalabilityServerPolicy extends SapphireServerPolicy {
		public ScalabilityGroupPolicy group;
		public long duration = 1000000; //duration between calls estimate measured in nano-seconds (default to 1 second)
		public static final double loadThreshold = 1.2/10000;
		public static final double learnRate = 0.1;
		public long lastCall = System.nanoTime();
		private String lock = new String("lock");
		private double maxLoad = 0;

		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (ScalabilityGroupPolicy)group;
			System.out.println("loadThreshhold = " + loadThreshold);
		}

		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}

		@Override
		public void onMembershipChange() {
		}

		@Override
		public Object onRPC(String method, Object[] params) throws Exception {
			// TODO: more fancy stuff - queue of requests ...
			synchronized(lock) {
				long now = System.nanoTime();
				duration = (long) ((1-learnRate) * duration + learnRate * (now-lastCall));
				lastCall = now;

				if (maxLoad < this.load())
					maxLoad = this.load();

				if(this.load() > loadThreshold) {
					//count++;
					duration = (long) 1.50 * duration;
					//group.addServer(this.scalabilityReplicate());
					throw new ObjectOverloadedException();
				}
			}

			return sapphire_getAppObject().invoke(method, params);
		}

		public double load() {
			return 1.0/duration;
		}

		public double getMaxLoad() {
			return maxLoad;
		}

		public ScalabilityServerPolicy scalabilityReplicate() {
			return (ScalabilityServerPolicy) sapphire_replicate();
		}

		public void scalabilityPin(String region) throws RemoteException {
			sapphire_pin(region);
		}
	}

	public static class ScalabilityGroupPolicy extends SapphireGroupPolicy {
		ArrayList<ScalabilityServerPolicy> servers = new ArrayList<ScalabilityServerPolicy>();
		private int no_servers = 4;

		@Override
		public void onCreate(SapphireServerPolicy server) {
			ScalabilityServerPolicy firstServer = (ScalabilityServerPolicy) server;
			servers.add(firstServer);
			try {
				ArrayList<String> regions = sapphire_getRegions();
				firstServer.scalabilityPin(regions.get(0));
				for (int i = 1; (i < no_servers) && (i < regions.size()); i++) {
					ScalabilityServerPolicy replica = firstServer.scalabilityReplicate();
					replica.scalabilityPin(regions.get(i));
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				throw new Error("Could not create new Scalability group policy.");
			}
		}

		@Override
		public void addServer(SapphireServerPolicy server) {
			servers.add((ScalabilityServerPolicy)server);
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
			int minInd = 0;
			double minVal = Double.MAX_VALUE;
			for(int i = 0; i < servers.size(); i++) {
				double cur = servers.get(i).load();
				if(cur < minVal) {
					minVal = cur;
					minInd = i;
				}
			}
			return servers.get(minInd);
		}
	}
}
