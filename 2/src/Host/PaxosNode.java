package Host;


import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Queue;
import java.util.TreeMap;

import state.PaxosState;
import data.LockAction;
import data.PaxosMessage;
import data.Proj2Message;
import data.Proj2Message.Command;


/**
 * Represents a node in a distributed paxos system. Designed to be the backbone of a distributed lock service.
 *
 */
public class PaxosNode extends Proj2Node{
	private TreeMap<Integer, PaxosState> roundState;
	
	// static, because we assume one PaxosNode per process
	public static int currentRound;
	public static int nid, clock; 
	public static Queue<LockAction> requests;
	
	public PaxosNode(int nodeId) throws UnknownHostException, SocketException {
		super();
		roundState = new TreeMap<Integer, PaxosState>();
		currentRound = 0; // for multi-paxos
		clock = 0; // for proposal #'s
		nid = nodeId;
	}
	
	public void run() throws IOException {	
		msgSocket = new DatagramSocket(nid);
		System.out.println("PaxosNode started! Listening on port "+nid);

		// keep receiving messages
		while(true){
			Proj2Message msg = receiveMessage();
            
            // increment the local clock
            clock = Math.max(clock, msg.clockVal) + 1;
            
            if (msg.command == Command.LOCK_SERVICE_REQUEST){
            	//update the queue
            } else {
            	//Check the learner to see if the value has already been learned. 
            }
            PaxosState ps = roundState.get(currentRound);//TODO should replace this with the correct round info
            if(ps == null){
            	ps = new PaxosState();
            	roundState.put(currentRound, ps);
            }
            //TODO handle each request based on the command
            switch(msg.command) {
            
	            case PREPARE:
	            case ACCEPT_REQUEST:
	        		if (!(msg.data instanceof PaxosMessage)){
	        			System.err.println("Acceptor: Received message data is not an "
	        					+ "instance of PaxosMessage");
	        			break;
	        		}
		            List<Proj2Message> respMsgs = ps.acceptor.handleMessage(msg);
		            if(respMsgs == null){
	                	//error, ignore
	                }else{
	                	for(Proj2Message respMsg : respMsgs){
	    	                if (respMsg.to == 0){
	    	                	sendToAllPaxos(respMsg);
	    	                }else{
	    	                	sendMessage(respMsg, respMsg.to);
	    	                }
	                	}
	                }
		            break;
	            case LOCK_SERVICE_REQUEST:
	            	//TODO 
	            case PROMISE:
	            case ACCEPTED:
	            	Proj2Message respMsg = ps.proposer.handleMessage(msg);
	                if(respMsg == null){
	                	//error, ignore
	                }
	                //send response message
	                if (respMsg.to == 0){
	                	sendToAllPaxos(respMsg);
	                }else{
	                	sendMessage(respMsg, respMsg.to);
	                }
	            	break;
	            case LEARN:
	            	Proj2Message learnerMsg = ps.learner.handleMessage(msg);
	            	if(learnerMsg == null){
	                	//error, ignore
	                }
	                //send response message
	                if (learnerMsg.to == 0){
	                	sendToAllPaxos(learnerMsg);
	                }else{
	                	sendMessage(learnerMsg, learnerMsg.to);
	                }
	            	break;
            	default:
            		System.out.println("Paxos got message: " + msg);
            }

            // TODO remove test code
            Proj2Message resp = new Proj2Message();
            resp.clockVal = clock;
            resp.from = nid;
            resp.command = Proj2Message.Command.LOCK_SERVICE_RESPONSE;
            
            sendMessage(resp, msg.from);
		}
	}
	
	private void sendToAllPaxos(Proj2Message msg) throws UnknownHostException, IOException {
		for(int node : PAXOS_MEMBERS) {
			sendMessage(msg, node);
		}
	}
	
	/**
	 * This is called by learner when the learner learned something.
	 * @param action The value being learned
	 */
	public static void valueLearned(LockAction action){
		//TODO check if the learned value is from the top of the task queue
		// This means this value is proposed by the proposer in this server.
		
		//TODO propose the next value in the task queue. 
	}
}