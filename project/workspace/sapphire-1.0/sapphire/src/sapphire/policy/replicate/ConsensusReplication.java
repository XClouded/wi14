package sapphire.policy.replicate;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import sapphire.common.SapphireObjectNotAvailableException;
import sapphire.common.SapphireObjectNotFoundException;
import sapphire.policy.SapphirePolicy;
import sapphire.policy.SapphirePolicy.SapphireClientPolicy;
import sapphire.policy.SapphirePolicy.SapphireGroupPolicy;
import sapphire.policy.SapphirePolicy.SapphireServerPolicy;

public class ConsensusReplication extends SapphirePolicy {
	
	public static class View implements Serializable {
		static final long serialVersionUID = 1L;

		int viewNum;
		ArrayList<ConsensusReplicateServerPolicy> serverGroup;
		ConsensusReplicateServerPolicy leader;
	}
	
	public static class ConsensusReplicateClientPolicy extends SapphireClientPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private ConsensusReplicateServerPolicy server;
		private ConsensusReplicateGroupPolicy group;
		
		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (ConsensusReplicateGroupPolicy) group;
		}
		
		@Override
		public void setServer(SapphireServerPolicy server) {
			this.server = (ConsensusReplicateServerPolicy) server;
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
			//Random r = new Random();
			//if(r.nextDouble() > 0.9) {
			//	group.onFailure(server);
			//	server = (ConsensusReplicateServerPolicy) group.onRefRequest();
			//}
			Object ret = server.onRPC(method, params); //TODO: change servers if our server fails
			return ret;
		}
	}
	
	/**
	 * Server Policy. Represents the object as a replicated state machine
	 * where order of operations is determined by paxos. Cheap paxos is used
	 * (assuming correct order of nodes in group) with the proposer acting
	 * as the distinguished learner for the round.
	 * @author ackeri
	 *
	 */
	public static class ConsensusReplicateServerPolicy extends SapphireServerPolicy {

		private static final long serialVersionUID = 1L;
		
		private ConsensusReplicateGroupPolicy group;
		
		private View view;
		int rpcNum = 0;
		int quorum = Integer.MAX_VALUE;
		boolean isLeading = false;
		
		public Object onRPC(String method, Object[] params) throws Exception {
			//System.out.println("marshalling call to " + method);
			while(true) {
				try {
					return view.leader.leaderOnRPC(view.viewNum,method, params);
				} catch(SapphireObjectNotAvailableException ex) {}
				System.out.println("trying again");
			}
		}
		
		public synchronized Object leaderOnRPC(int vNum, String method, Object[] params) throws Exception {
			//System.out.println("leader recieved call to " + method);
			if(!isLeading)
				throw new SapphireObjectNotAvailableException("Contacted server was not current leader, view out of sync");
			if(vNum != view.viewNum)
				throw new SapphireObjectNotAvailableException("your view is out of date");
			ArrayList<ConsensusReplicateServerPolicy> servants = new ArrayList<ConsensusReplicateServerPolicy>();
			for(ConsensusReplicateServerPolicy s : view.serverGroup) {
				Boolean response = null;
				try {
					response = s.getPermission(view.viewNum);
				} catch (Exception ex) {
					//could not contact the respondent, optimistically hope we can contact a majority
					//System.out.println("couldn't contact server: " + ex);
				}
				if(response == null) {
					
				}else if(response) {
					servants.add(s);
					if(servants.size() >= quorum)
						break;
					//System.out.println("positive response");
				} else {
					view = group.getView();
					throw new SapphireObjectNotAvailableException("Contacted leader was out of date, please try request again");
				}
			}
			if(servants.size() >= quorum) {
				//System.out.println("successfully handled call");
				rpcNum++;
				Object ret = sapphire_getAppObject().invoke(method, params);
				Serializable state = sapphire_getAppObject().getObject();
				for(ConsensusReplicateServerPolicy s : servants) {
					try {
					s.setObject(vNum,state,rpcNum);
					} catch(Exception ex) {System.out.println("couldn't push to replica " + ex);}
				}
				return ret;
			}
			throw new SapphireObjectNotAvailableException("Couldn't contact a majority of replicas");
		}

		public void setObject(int vNum, Serializable object, int rpcNum) {
			//System.out.println("in setobject has view " + view);
			//if(view == null)
			//	System.out.println("My View is null so I can't participate");
			//System.out.println("Someone trying to set my object me: " + view.viewNum + " " + this.rpcNum + " them: " + vNum + " " + rpcNum);
			if(view != null && vNum == view.viewNum) {
				//System.out.println("accepting new version after " + rpcNum);
				this.sapphire_getAppObject().setObject(object);
				this.rpcNum = rpcNum;
			}
		}

		public boolean getPermission(int vNum) {
			//System.out.println("has view " + view);
			if(view != null && vNum == view.viewNum) {
				return true; //we assume only the leader will try to get permission
			} else {
				if(view == null || vNum > view.viewNum) {
					//System.out.println("replica out of date, updating with group");
					view = group.getView();
					return getPermission(vNum);
				}
				return false;
			}
		}

		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (ConsensusReplicateGroupPolicy) group;
		}
		
		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}
		
		@Override
		public void onMembershipChange() {
			
		}
		
		public ConsensusReplicateServerPolicy ConsensusReplicate() {
			return (ConsensusReplicateServerPolicy) this.sapphire_replicate();
		}
		
		public void ConsensusReplicatePin(String region) throws RemoteException {
			System.out.println("Server moving to " + region);
			this.sapphire_pin(region);
		}

		public synchronized void setView(View view) {
			//System.out.println("Recieving new view " + view);
			isLeading = false;
			this.view = view;
		}

		public void beginLeading() throws SapphireObjectNotAvailableException {
			//System.out.println("begineLeading");
			int contacted = 0;
			int mostRecent = -1;
			quorum = view.serverGroup.size()/2 +1;
			ConsensusReplicateServerPolicy mostUpdated = null;
			for(ConsensusReplicateServerPolicy s : view.serverGroup) {
				try {
					int res = s.getRPCNum();
					System.out.println("contacted server with rpcnum " + res);
					if(mostRecent < res) {
						mostRecent = res;
						mostUpdated = s;
					}
					contacted++;
				} catch(Exception ex) {}
			}
			if(!(contacted >= view.serverGroup.size()/2 + 1))
				throw new SapphireObjectNotAvailableException("couldn't verify was up to date");
			if(mostUpdated != null && mostRecent > rpcNum)
				this.sapphire_getAppObject().setObject(mostUpdated.getObject());
			isLeading = true;
		}

		public Serializable getObject() {
			//System.out.println("in getObject " + this.sapphire_getAppObject());
			return this.sapphire_getAppObject().getObject();
		}

		public  int getRPCNum() {
			return rpcNum;
		}

	}
	
	public static class ConsensusReplicateGroupPolicy extends SapphireGroupPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private View view;
		
		@Override
		public void onCreate(SapphireServerPolicy server) {
			System.out.println("In group onCreate");
			view = new View();
			view.viewNum = 0;
			view.serverGroup = new ArrayList<ConsensusReplicateServerPolicy>();

			//TODO: this is arbitrary
			ConsensusReplicateServerPolicy serv = (ConsensusReplicateServerPolicy) server;
			try {
			ArrayList<String> regions = this.sapphire_getRegions();
			view.serverGroup.add(serv);
			view.serverGroup.add(serv.ConsensusReplicate());
			view.serverGroup.add(serv.ConsensusReplicate());
			view.leader = view.serverGroup.get(view.serverGroup.size()-1);
			
			System.out.println(regions);
			view.serverGroup.get(0).ConsensusReplicatePin(regions.get(0));
			view.serverGroup.get(1).ConsensusReplicatePin(regions.get(1));
			view.serverGroup.get(2).ConsensusReplicatePin(regions.get(2));
			} catch(Exception ex) {System.out.println("SETUP FAILED: " + ex);}//assume this works
			
			informMembers();
		}

		public View getView() {
			return view;
		}

		@Override
		public void addServer(SapphireServerPolicy server) {
			System.out.println("Someone is calling addServer with " + server);
			
		}

		@Override
		public ArrayList<SapphireServerPolicy> getServers() {
			return new ArrayList<SapphireServerPolicy>(view.serverGroup);
		}

		@Override
		public void onFailure(SapphireServerPolicy server) {
			System.out.println("failure of node");
			view.serverGroup.remove(server);
			
			view.viewNum += 1;
			ConsensusReplicateServerPolicy serv = view.serverGroup.get(0).ConsensusReplicate();
			view.serverGroup.add(serv);
			view.leader = view.serverGroup.get(view.serverGroup.size()-1);
			
			informMembers();
		}

		private void informMembers() {
			//System.out.println("GROUP: pushing new view " + view.viewNum);
			for(int i = 0; i < view.serverGroup.size(); i++) {
				try {
					//System.out.println("GROUP: informing server");
					view.serverGroup.get(i).setView(view);
				} catch(Exception ex) {System.out.println("GROUP: push failed");}
			}
			try {
				//System.out.println("GROUP: informing leader");
				view.leader.beginLeading();
			} catch(Exception ex) {
				this.onFailure(view.leader);
			}
		}
		
		@Override
		public SapphireServerPolicy onRefRequest() {
			return view.leader;
		}
		
	}
}
