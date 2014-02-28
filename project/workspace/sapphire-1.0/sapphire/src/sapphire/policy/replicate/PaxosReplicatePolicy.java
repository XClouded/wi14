package sapphire.policy.replicate;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;

import sapphire.common.AppObject;
import sapphire.common.SapphireObjectNotAvailableException;
import sapphire.policy.SapphirePolicy;

/**
 * A replication policy, using paxos to determine order of RPC's
 * @author ackeri
 *
 */
public class PaxosReplicatePolicy extends SapphirePolicy {

	public enum StateType {
		UPDATE,DECIDED,DECIDING
	}
	
	public interface RoundState {
		public StateType getStateType();
	}
	
	public static class Tuple {
		public Object first;
		public Object second;
		public Tuple(Object f, Object s) {first = f; second = s;}
		public Tuple() {
			// TODO Auto-generated constructor stub
		}
	}
	
	public static class Proposal implements Serializable,RoundState {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int propNum;
		private RPC value;
		
		public Proposal() {this.propNum = -1; this.value = null;}
		public Proposal(int propNum, RPC value) {this.propNum = propNum; this.value = value;}
		
		public int getPropNum() {return propNum;}
		public RPC getValue() {return value;}
		
		public StateType getStateType() {return StateType.DECIDING;}

		public String toString() {
			return "PROPOSAL " + propNum + " "+ value;
		}
	}
	
	public static class RPC implements Serializable,RoundState {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int id;	//must be unique
		private String method;
		private Object[] params;
		
		public RPC(int id,String method, Object[] params) {this.id = id; this.method = method; this.params = params;}
		
		public int getId() {return id;}
		public String getMethod() {return method;}
		public Object[] getParams() {return params;}

		public StateType getStateType() {return StateType.DECIDED;}

		public String toString() {
			return "VALUE " + method;
		}
	}
	
	public static class State implements Serializable,RoundState {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private AppObject state;
		private int round;
		
		public State(int round, AppObject state) {this.round = round; this.state = state;}

		public AppObject getState() {return state;}
		public int getRound() {return round;}

		public StateType getStateType() {return StateType.UPDATE;}

		public Object update(RPC value) throws Exception {
			round++;
			return state.invoke(value.getMethod(),value.getParams());
		}

		public String toString() {
			return "STATE " + state.toString();
		}
	}

	public static class PaxosReplicateClientPolicy extends SapphireClientPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private PaxosReplicateServerPolicy server;
		private PaxosReplicateGroupPolicy group;
		
		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (PaxosReplicateGroupPolicy) group;
		}
		
		@Override
		public void setServer(SapphireServerPolicy server) {
			this.server = (PaxosReplicateServerPolicy) server;
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
			//server = (PaxosReplicateServerPolicy)group.getServers().get(0);
			//System.out.println("Received call to " + method);
			Object ret = server.onRPC(method, params);
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
	public static class PaxosReplicateServerPolicy extends SapphireServerPolicy {
		//static private Logger logger = Logger.getLogger("sapphire.policy.replicate.PaxosReplicateServerPolicy");
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private class PaxosState implements Serializable {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private State state;
			private Map<Integer,Integer> promises;
			private Map<Integer,RoundState> states;
			private int highest;

			public PaxosState() {
				promises = new HashMap<Integer,Integer>();
				states = new HashMap<Integer,RoundState>();
				highest = 0;
				state = new State(0, sapphire_getAppObject());
			}
			
			public synchronized RoundState prepare(int round, int propNum) {
				if(round < state.getRound()) {
					return state;
				}
				if(promises.containsKey(round) && propNum < promises.get(round)) {
					return null;
				}
				promises.put(round,propNum);
				if(states.get(round) == null)
					states.put(round, new Proposal());
				RoundState ret = states.get(round);
				return ret;
			}
			
			public synchronized boolean accept(int round, Proposal prop) {
				if(promises.containsKey(round) && prop.getPropNum() < promises.get(round)) {
					return false;
				}
				states.put(round, prop);
				return true;
			}
			
			public synchronized Object learn(int round, RPC value) throws Exception{
				states.put(round,value);
				if(round >= highest) {
					highest = round + 1;
				}
				return getValue(round);
			}
			
			public synchronized Object getValue(int round) throws Exception {
				if(state.getRound() > round + 1)
					throw new SapphireObjectNotAvailableException("Cannot do to garbage collected data "+state.getRound());
				Object ret = null;
				boolean evaluated = false;
				for(int i = state.getRound(); i <= round; i++) {
					RoundState s = states.get(i);
					if(s == null)
						throw new SapphireObjectNotAvailableException("Did not have value for round "+i);
					switch(s.getStateType()) {
						case DECIDING:
							throw new SapphireObjectNotAvailableException("Have an undecided round, stuck at "+i);
						case DECIDED:
							ret = state.update((RPC)s);
							evaluated = true;
							break;
						case UPDATE:
							state = (State)s;
							evaluated = false;
							break;
					}
					states.remove(i);
					promises.remove(i);
				}
				if(!evaluated)
					throw new SapphireObjectNotAvailableException("Did not have RPC value for last round");
				return ret;
			}

			public synchronized void update(State s) {
				if(s.getRound() >= state.getRound()) {
					state = s;
					highest = s.getRound();
				}
			}
			
			public synchronized int getVersion() {
				return highest;
			}
		}
		
		private PaxosReplicateGroupPolicy group;
		
		private PaxosState paxosState;
		private List<PaxosReplicateServerPolicy> paxosGroup;
		private int quorum;
		private int serverNum;
		private int nextRPC;

		
		public void setServers(List<PaxosReplicateServerPolicy> paxosGroup2, int server) {
			paxosState = new PaxosState();
			paxosGroup = paxosGroup2;
			quorum = (int)Math.floor(paxosGroup.size()/2 + 1);
			serverNum = server;
			nextRPC = server;
		}
		
		public synchronized Object onRPC(String method, Object[] params) throws Exception {
			//try to propose in current round
			Set<Integer> responders = new HashSet<Integer>();
			int propNum = serverNum;
			int round = paxosState.getVersion();
			int ourRPC = nextRPC;
			nextRPC += paxosGroup.size();
			Proposal prop = doPrepare(round,propNum,responders);
			while(prop == null) {
				responders.clear();
				propNum += paxosGroup.size();
				round = paxosState.getVersion();
				prop = doPrepare(round,propNum,responders);
			}
			
			//Convert to proposal we will make
			RPC val = prop.getValue();
			if(val == null)
				val =  new RPC(ourRPC,method,params);
			prop = new Proposal(propNum,val);
			
			//send out accepts (proposing to exactly a quorum, so need all to accept)
			boolean accepted = true;
			for(int i : responders) {
				try {
				accepted &= paxosGroup.get(i).accept(serverNum,round,prop);
				} catch(Exception e) {accepted = false; break;}
			}
			boolean evaluated = false;
			if(accepted) {
				Object ret = null;
				State cur = null;
				boolean isFirst = true;
				for(int i : responders) {
					try {
						Tuple temp = paxosGroup.get(i).learn(round,val,isFirst,cur);
						if(temp != null) {
							ret = temp.first;
							cur = (State)temp.second;
						}
						if(isFirst)
							evaluated = true;
						isFirst = false; //make sure to never evaluate twice
					//ignore connection exceptions
					} catch(RemoteException e){} catch(SapphireObjectNotAvailableException e){}
				}

				if(evaluated && prop.getValue().getId() == ourRPC) {
					return ret;
				}
			}
			throw new SapphireObjectNotAvailableException("Could not successfully propose " + (!accepted ? "not accepted":(!evaluated? "not evaluated":"not the right RPC"))); 
		}
		
		private Proposal doPrepare(int round, int propNum, Set<Integer> responders) {
			Proposal toPropose = new Proposal(-1,null);
			for(int i = 0; i < paxosGroup.size() && responders.size() < quorum; i++) {
				try {
					RoundState prop = paxosGroup.get(i).prepare(serverNum, round,propNum);
					if(prop == null) {										//was unwilling
						//do nothing (and hope we get enough)
						if((paxosGroup.size()-i)+responders.size()-1 < quorum)
							return null;
					} else if(prop.getStateType() == StateType.DECIDING){	//received promise
						Proposal temp = (Proposal)prop;
						if(temp.getPropNum() > toPropose.getPropNum()) {
							toPropose = temp;
						}
						responders.add(i);
					} else if(prop.getStateType() == StateType.DECIDED) {	//round is already decided
						//paxosState.learn(round, (RPC)prop);
						//TODO: we can't call learn here, so we gain no new information.
						return null;
					} else {												//we are way behind
						paxosState.update((State)prop);
						return null;
					}
				} catch(Exception e) {
					if((paxosGroup.size()-i)+responders.size()-1 < quorum)
						return null;
				}
			}
			if(responders.size() >= quorum)
				return toPropose;
			return null;
		}
		
		public RoundState prepare(int source,int round, int propNum) {
			RoundState ret = paxosState.prepare(round, propNum);
			return ret;
		}
		
		public boolean accept(int source,int round, Proposal prop) {
			boolean ret = paxosState.accept(round, prop);
			return ret;
		}
		
		public Tuple learn(int round, RPC value, Boolean isFirst, State newState) throws Exception {
			if(isFirst) {
				Tuple ret = new Tuple();
				ret.first = paxosState.learn(round, value);
				ret.second = paxosState.state;
				return ret;
			}
			
			paxosState.update(newState);
			return null;
		}

		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (PaxosReplicateGroupPolicy) group;
		}
		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}
		@Override
		public void onMembershipChange() {
			// TODO Auto-generated method stub
			
		}
		
		public List<PaxosReplicateServerPolicy> startServers() {
			paxosGroup = new ArrayList<PaxosReplicateServerPolicy>();
			paxosGroup.add((PaxosReplicateServerPolicy)sapphire_replicate());
			//paxosGroup.add((PaxosReplicateServerPolicy)sapphire_replicate());
			paxosGroup.add((PaxosReplicateServerPolicy)this);
			
			for(int i = 0; i < paxosGroup.size(); i++)
				paxosGroup.get(i).setServers(paxosGroup, i);
			return paxosGroup;
		}
		
		public PaxosReplicateServerPolicy paxosReplicate() {
			return (PaxosReplicateServerPolicy) this.sapphire_replicate();
		}
		
		public void paxosReplicatePin(String region) throws RemoteException {
			System.out.println("Server " + serverNum + " moving to " + region);
			sapphire_pin(region);
		}

	}
	
	public static class PaxosReplicateGroupPolicy extends SapphireGroupPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		List<PaxosReplicateServerPolicy> paxosGroup;
		@Override
		public void onCreate(SapphireServerPolicy server) {
			System.out.println("In group onCreate");
			PaxosReplicateServerPolicy serv = (PaxosReplicateServerPolicy) server;
			//paxosGroup = new ArrayList<PaxosReplicateServerPolicy>();
			//paxosGroup.add(serv);
			//serv.setServers(paxosGroup,0);
			
			//find client region
			String region = null;
			try {
//				for(String s : this.sapphire_getRegions()) {
//					System.out.println(s);
//					if(s.startsWith("pitfall")) {
//						client = s;
//					}
//				}
				region = sapphire_getRegions().get(0);
			} catch (RemoteException e) {e.printStackTrace();}
			
			paxosGroup = new ArrayList<PaxosReplicateServerPolicy>();
			paxosGroup.add(serv);
			paxosGroup.add(serv.paxosReplicate());
			paxosGroup.add(serv.paxosReplicate());
			
			//try {System.out.println("moving server 0 to " + this.sapphire_getRegions().get(1));
			//paxosGroup.get(0).paxosReplicatePin(this.sapphire_getRegions().get(1));} catch (RemoteException e) {e.printStackTrace();}
			//try {System.out.println("moving server 1 to " + this.sapphire_getRegions().get(2));
			//paxosGroup.get(1).paxosReplicatePin(this.sapphire_getRegions().get(2));} catch (RemoteException e) {e.printStackTrace();}
			try {paxosGroup.get(0).paxosReplicatePin(region);} catch (RemoteException e) {e.printStackTrace();}
			try {paxosGroup.get(1).paxosReplicatePin(region);} catch (RemoteException e) {e.printStackTrace();}

			//paxosGroup = serv.startServers();
			for(int i = 0; i < paxosGroup.size(); i++) {
				paxosGroup.get(i).setServers(paxosGroup, i);
			}
		}

		@Override
		public void addServer(SapphireServerPolicy server) {
			// TODO Auto-generated method stub
			System.out.println("Someone is calling addServer with " + server);
			
		}

		@Override
		public ArrayList<SapphireServerPolicy> getServers() {
			return new ArrayList<SapphireServerPolicy>(paxosGroup);
		}

		@Override
		public void onFailure(SapphireServerPolicy server) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
