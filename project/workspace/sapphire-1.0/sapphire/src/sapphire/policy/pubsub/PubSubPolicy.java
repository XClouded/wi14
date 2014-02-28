package sapphire.policy.pubsub;

import java.util.ArrayList;

import sapphire.policy.SapphirePolicy;
import sapphire.policy.dht.DHTPolicy.DHTGroupPolicy;
import sapphire.policy.dht.DHTPolicy.DHTServerPolicy;

public class PubSubPolicy extends SapphirePolicy {

	public static class PubSubClientPolicy extends SapphireClientPolicy {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		PubSubServerPolicy server = null;
		PubSubGroupPolicy group = null;

		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (PubSubGroupPolicy) group;
		}

		@Override
		public void setServer(SapphireServerPolicy server) {
			this.server = (PubSubServerPolicy) server;
		}

		@Override
		public SapphireServerPolicy getServer() {
			return server;
		}

		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}
	}

	public static class PubSubServerPolicy extends SapphireServerPolicy {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private PubSubGroupPolicy group = null;

		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (PubSubGroupPolicy) group;
		}

		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}

		@Override
		public void onMembershipChange() {
		}
	}

	public static class PubSubGroupPolicy extends SapphireGroupPolicy {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void onCreate(SapphireServerPolicy server) {
			// TODO Auto-generated method stub

		}

		@Override
		public void addServer(SapphireServerPolicy server) {
			// TODO Auto-generated method stub

		}

		@Override
		public ArrayList<SapphireServerPolicy> getServers() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void onFailure(SapphireServerPolicy server) {
			// TODO Auto-generated method stub

		}
	}
}
