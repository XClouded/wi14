package sapphire.policy;

import java.util.ArrayList;

public class DefaultSapphirePolicy extends SapphirePolicy {
	
	public static class DefaultServerPolicy extends SapphireServerPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private DefaultGroupPolicy group;
		
		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}

		@Override
		public void onMembershipChange() {}

		@Override
		public void onCreate(SapphireGroupPolicy group) {
			// TODO Auto-generated method stub
			this.group = (DefaultGroupPolicy) group;
		}
	}
	
	public static class DefaultClientPolicy extends SapphireClientPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private DefaultServerPolicy server;
		private DefaultGroupPolicy group;
		
		@Override
		public void setServer(SapphireServerPolicy server) {
			this.server = (DefaultServerPolicy) server;
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
			// TODO Auto-generated method stub
			this.group = (DefaultGroupPolicy) group;
		}
	}
	
	public static class DefaultGroupPolicy extends SapphireGroupPolicy {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void addServer(SapphireServerPolicy server) {}

		@Override
		public void onFailure(SapphireServerPolicy server) {}

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
			// TODO Auto-generated method stub
			
		}
		
	}
}
