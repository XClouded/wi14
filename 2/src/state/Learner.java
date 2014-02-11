package state;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import Host.PaxosNode;
import data.LockAction;
import data.PaxosMessage;
import data.Proj2Message;
import data.Proj2Message.Command;

public class Learner implements Serializable{
	private static final long serialVersionUID = 1L;
	public LockAction learnedValue;
	public Map<Integer, LockAction> nidToValue;
	
	public Learner(){
		learnedValue = null;
		nidToValue = new HashMap<Integer, LockAction>();
	}
	
	/**
	 * 
	 * @param msg
	 * @return message that is needed to sent. Null if there is already a learned 
	 * value of the message body is not an instance of PaxosMessage
	 */
	public Proj2Message handleMessage(Proj2Message msg){
		Proj2Message resp = null;
		switch(msg.command) {
		case LEARN:
			System.out.println("ASKED TO LEARN: " + ((PaxosMessage)msg.data).value.toString());
			if (learnedValue != null) return null;//ignore if there is already something learned. 
			if(!(msg.data instanceof PaxosMessage)){
				System.err.println("Received message data is not an "
						+ "instance of PaxosMessage");
				return null;
			}
			PaxosMessage pm = (PaxosMessage)msg.data;
			nidToValue.put(msg.from, pm.value);
			if(nidToValue.size() >= PaxosNode.MAJORITY_SIZE){
				Map<LockAction, Integer> actionVote = new HashMap<LockAction, Integer>();
				for(int nid : nidToValue.keySet()){
					LockAction la = nidToValue.get(nid);
					if(!actionVote.containsKey(la)){
						actionVote.put(la, 1);
					}else {
						actionVote.put(la, actionVote.get(la) + 1);
					}
					if(actionVote.get(la) >= PaxosNode.MAJORITY_SIZE){ //value learned
						learnedValue = la;
						System.out.println("LEARNED THIS: " + la.toString());
						//notify client
						resp = new Proj2Message();
						resp.command = Command.LOCK_SERVICE_RESPONSE;
						resp.data = pm.value;
						resp.from = PaxosNode.nid;
						resp.to = pm.value.client;
						break;
					}
				}
			}
			
        	break;
    	default:
    		System.err.println("Wrong Message type passed into learner");
		}
		return resp;
	}
}
