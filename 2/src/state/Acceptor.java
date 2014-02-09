package state;

import java.io.Serializable;

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

	public Proj2Message handleMessage(Proj2Message msg) {
		// TODO Auto-generated method stub
		Proj2Message result = new Proj2Message();

		PaxosMessage pmReceived = (PaxosMessage)msg.data;
		switch(msg.command) {
        
        case PREPARE:
        	result.command = Command.PROMISE;
        	result.to = msg.from;
        	result.from = PaxosNode.nid;
        	PaxosMessage pm = new PaxosMessage(pmReceived.paxosRound, 
        			Math.max(highestNumProposal.proposalNum, pmReceived.proposalNum), 
        			highestNumProposal.value);
        	result.data = pm;
        	break;
        case ACCEPT_REQUEST:
        	if (pmReceived.proposalNum <= highestNumProposal.proposalNum){ //can not accept
        		result.command = Command.PROMISE;
        		result.data = highestNumProposal;
        	}else {
        		result.command = Command.ACCEPTED;
        		result.data = pmReceived;
        		highestNumProposal = pmReceived;
        	}
        	result.to = msg.from;
        	result.from = PaxosNode.nid;
            break;
    	default:
    		System.out.println("Paxos got message: " + msg);
    }
		return null;
	}
}
