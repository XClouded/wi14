package state;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import Host.PaxosNode;
import data.PaxosMessage;
import data.Proj2Message;
import data.Proj2Message.Command;

public class Acceptor implements Serializable{
	private static final long serialVersionUID = 1L;
	PaxosMessage highestNumProposal;

	public Acceptor(){
		highestNumProposal = new PaxosMessage(PaxosNode.currentRound, Integer.MIN_VALUE);
	}

	public List<Proj2Message> handleMessage(Proj2Message msg) {
		// TODO Auto-generated method stub
		List<Proj2Message> results = new LinkedList<Proj2Message>();

		PaxosMessage pmReceived = (PaxosMessage)msg.data;
		Proj2Message toProposer = new Proj2Message();
		switch(msg.command) {
        
        case PREPARE:
        	
        	toProposer.command = Command.PROMISE;
        	toProposer.to = msg.from;
        	toProposer.from = PaxosNode.nid;
        	PaxosMessage pm = new PaxosMessage(pmReceived.paxosRound, 
        			Math.max(highestNumProposal.proposalNum, pmReceived.proposalNum), 
        			highestNumProposal.value);
        	toProposer.data = pm;
        	results.add(toProposer);
        	break;
        case ACCEPT_REQUEST:
        	
        	if (pmReceived.proposalNum <= highestNumProposal.proposalNum){ //can not accept
        		toProposer.command = Command.PROMISE;
        		toProposer.data = highestNumProposal;
        	}else { // accept the proposed value
        		System.out.println("ACCEPTING: " + ((PaxosMessage)msg.data).value.toString());
        		toProposer.command = Command.ACCEPTED;
        		toProposer.data = pmReceived;
        		highestNumProposal = pmReceived;
        		
        		Proj2Message toLearnner = new Proj2Message();
        		toLearnner.command = Command.LEARN;
        		toLearnner.data = pmReceived;
        		toLearnner.from = PaxosNode.nid;
        		toLearnner.to = -1; //to broadcast to all acceptors.
        		results.add(toLearnner);
        	}
        	toProposer.to = msg.from;
        	toProposer.from = PaxosNode.nid;
        	results.add(toProposer);
        	
            break;
    	default:
    		System.out.println("Paxos got message: " + msg);
    		return null;
		}
		
		return results;
	}
}
