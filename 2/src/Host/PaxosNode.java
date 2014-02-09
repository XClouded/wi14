package Host;


import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.TreeMap;

import data.LockAction;
import data.Proj2Message;
import state.PaxosState;


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
	
	public PaxosNode(String[] args) throws UnknownHostException, SocketException {
		super();
		roundState = new TreeMap<Integer, PaxosState>();
		currentRound = 0; // for multi-paxos
		clock = 0; // for proposal #'s
		nid = 0;
		
		if (args.length < 1) {
			System.out.println("Must specify port to listen on");
			System.out.println("e.g. 'PaxosNode 9001'");
			System.exit(1);
		}

		try {
			nid = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Unable to parse port: "+args[0]);
			System.exit(1);
		}
	}
	
	public void run() throws IOException {	
		msgSocket = new DatagramSocket(nid);
		System.out.println("PaxosNode started! Listening on port "+nid);

		// keep receiving messages
		while(true){
			Proj2Message msg = receiveMessage();
            
            // increment the local clock
            clock = Math.max(clock, msg.clockVal) + 1;
            
            PaxosState ps = roundState.get(currentRound);//TODO should replace this with the 
            Proj2Message respMsg = null;
            //TODO handle each request based on the command
            switch(msg.command) {
            
	            case PREPARE:
	            case ACCEPT_REQUEST:
		            ps.acceptor.handleMessage(msg);
		            break;
	            case LOCK_SERVICE_REQUEST:
	            	//TODO 
	            case PROMISE:
	            case ACCEPTED:
	            	respMsg = ps.proposer.handleMessage(msg);
	            	break;
	            case LEARN:
	            	ps.learner.handleMessage(msg);
	            	break;
            	default:
            		System.out.println("Paxos got message: " + msg);
            }
            if(respMsg == null){
            	//error, ignore
            }
            //send response message
            if (respMsg.to == 0){
            	sendToAllPaxos(respMsg);
            }else{
            	sendMessage(respMsg, respMsg.to);
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
}
