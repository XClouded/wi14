package state;

import java.io.Serializable;

import Host.PaxosNode;
import data.LockAction;
import data.Proj2Message;
import data.Proj2Message.Command;

public class Proposer implements Serializable{
	private static final long serialVersionUID = 1L;
	private enum State{
		IDLE, 
		PREPARING,
		ACCEPTING,
	}
	
	private State state;
	private int currentProposalNumber;
	
	public Proposer(){
		state = State.IDLE;
	}
	
	public Proj2Message handleMessage(Proj2Message msg){
		
		Proj2Message result = new Proj2Message();
		switch (msg.command){
		case LOCK_SERVICE_REQUEST:
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
			
			//create prepare message
			result.command = Command.PREPARE;
			currentProposalNumber = PaxosNode.nid;
			result.clockVal = nextProposalNum(currentProposalNumber);
			result.data = msg.data;
			
			//change proposer state
			state = State.PREPARING;
			break;
		case PROMISE:
			if(state != State.PREPARING){
				System.err.println("Proposer is not asking for promise now");
				return null;
			}
			
			break;
		case ACCEPTED:
			if(state != State.ACCEPTING){
				System.err.println("Proposer is not expecting any accepting message");
			}
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
		return currentProposalNum + PaxosNode.NODE_COUNT;
	}
	
	private void propose(LockAction value){
		
	}
	
	private void proposalResponse(){
		
	}
	

}
