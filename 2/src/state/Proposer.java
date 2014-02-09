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
		ACCEPTED,
	}
	
	private State state;
	private PaxosNode server;
	private int currentProposalNumber;
	
	
	public Proposer(PaxosNode server){
		state = State.IDLE;
		this.server = server;
	}
	
	public Proj2Message handleMessage(Proj2Message msg){
		
		Proj2Message result = new Proj2Message();
		switch (msg.command){
		case LOCK_REQUEST:
			if(state != State.IDLE){
				System.err.println("Can not propose new values while "
						+ "the current paxos instance is still running");
				return null;
			}
			if(!(msg.data instanceof LockAction)){
				System.err.println("");
				return null;
			}
			
			//create prepare message
			result.command = Command.PREPARE;
			currentProposalNumber = server.nid;
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
			break;
		default:
				
		}
		return result;
	}

	/*
	 * Generates the next proposal number from current proposal number
	 * This function is necessary since two different proposers from different server
	 * should not propose the same proposal number. 
	 */
	private int nextProposalNum(int currentProposalNum){
		return currentProposalNum + server.SERVER_COUNT;
	}
	
	private void propose(LockAction value){
		
	}
	
	private void proposalResponse(){
		
	}
	

}
