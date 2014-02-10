package state;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import Host.PaxosNode;
import Host.Proj2Node;
import data.LockAction;
import data.PaxosMessage;
import data.Proj2Message;
import data.Proj2Message.Command;

public class Proposer implements Serializable{
	private static final long serialVersionUID = 1L;
	private enum State{
		IDLE, 
		PREPARING,
		ACCEPTED,
	}
	
	private State state;
	private int currentProposalNumber;
	private Map<Integer, PaxosMessage> promisesReceived; //nid to promise
	private Proj2Message originalPropose;
	
	public Proposer(){
		state = State.IDLE;
		promisesReceived = new HashMap<Integer, PaxosMessage>();
	}
	
	public Proj2Message handleMessage(Proj2Message msg){
		
		Proj2Message result = null;
		switch (msg.command){
		case LOCK_SERVICE_REQUEST:
			result = new Proj2Message();
			if(state != State.IDLE){
				System.err.println("Can not propose new values while "
						+ "the current paxos instance is still running");
				return null;
			}
			//TODO client will send LocakAction as data right?
			if(!(msg.data instanceof LockAction)){
				System.err.println("Received message data is not an "
						+ "instance of LocakAction");
				return null;
			}
			originalPropose = msg;
			//create prepare message
			result.command = Command.PREPARE;
			currentProposalNumber = nextProposalNum(PaxosNode.nid);
			result.data = new PaxosMessage(PaxosNode.currentRound, 
										   currentProposalNumber, 
										   (LockAction)msg.data);
			result.to = -1;
			result.from = PaxosNode.nid;
			//change proposer state
			state = State.PREPARING;
			break;
		case PROMISE:
			
			if(state != State.PREPARING){
				System.err.println("Proposer is not asking for promise now. Ignore");
				return null;
			}
			if (!(msg.data instanceof PaxosMessage)){
				System.err.println("Received message data is not an "
						+ "instance of PaxosMessage");
				return null;
			}
			promisesReceived.put(msg.from, (PaxosMessage)msg.data);
			if (promisesReceived.size() >= PaxosNode.MAJORITY_SIZE){//got response from majority
				int highestPromisedNum = Integer.MIN_VALUE;
				LockAction action = null;
				for(int nid : promisesReceived.keySet()){
					PaxosMessage pm = promisesReceived.get(nid);
					if (pm.proposalNum > highestPromisedNum){
						highestPromisedNum = pm.proposalNum;
						action = pm.value;
					}
				}
				if(action == null){
					System.out.println("requests size: " + PaxosNode.requests.size());
					//no proposals from acceptors
					action = (LockAction) originalPropose.data;
				}
				currentProposalNumber = nextProposalNum(currentProposalNumber);
				result = new Proj2Message();
				result.command = Command.ACCEPT_REQUEST;
				result.data = new PaxosMessage(PaxosNode.currentRound, currentProposalNumber, action);
				result.to = -1;
				result.from = PaxosNode.nid;
			}
			break;
		case ACCEPTED:
			if(state != State.PREPARING){
				System.err.println("Proposer is not expecting any accepting message");
			}
			state = State.ACCEPTED;
			break;
		default:
			
			//TODO should not reach this point.
			System.err.println("Wrong Message type passed into proposer");
		}
		return result;
	}

	/*
	 * Generates the next proposal number from current proposal number
	 * This function is necessary since two different proposers from different server
	 * should not propose the same proposal number. 
	 */
	private int nextProposalNum(int currentProposalNum){
		return currentProposalNum + Proj2Node.NODE_COUNT;
	}
	
}
