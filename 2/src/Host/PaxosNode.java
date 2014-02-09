package Host;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import state.PaxosState;
import data.Proj2Message;


/**
 * Represents a node in a distributed paxos system. Designed to be the backbone of a distributed lock service.
 *
 */
public class PaxosNode {
	public static final int[] PAXOS_MEMBERS = {9002, 9003, 9004, 9005, 9006};
	public static final int SERVER_COUNT = PAXOS_MEMBERS.length;
	public static final int MAJORITY_COUNT = SERVER_COUNT / 2 + 1;
	
	public Map<Integer, PaxosState> roundState;
	public int nid = 0;
	public int currentRound = 0; // for multi-paxos
	public int clock = 0; // for proposal #'s
	
	public PaxosNode(int port) throws IOException{
		roundState = new HashMap<Integer, PaxosState>();
		nid = port;
		currentRound = 0;
		clock = 0;
		
		ServerSocket welcomeSocket = new ServerSocket(nid);
		
		while(true){
			Socket connectionSocket = welcomeSocket.accept();
			ObjectInputStream inFromClient = new ObjectInputStream(connectionSocket.getInputStream());
			Proj2Message msg = null;
			
			try {
				msg = (Proj2Message)inFromClient.readObject();
			} catch (ClassNotFoundException e) {
				System.err.println("Class not found exception, failed to cast message");
			}
			
			// close the incoming connection
            inFromClient.close();
            connectionSocket.close();
            
            // increment the local clock
            clock = Math.max(clock, msg.clockVal) + 1;
            
            //TODO handle each request based on the command
            PaxosState ps = roundState.get(currentRound);//TODO should replace this with the 
            switch(msg.command) {
            case PREPARE:
            case ACCEPT_REQUEST:
            	ps.acceptor.handleMessage(msg);
            	break;
            case LOCK_REQUEST:
            case PROMISE:
            case ACCEPTED:
            	ps.proposer.handleMessage(msg);
            	break;
            case LEARN:
            	ps.learner.handleMessage(msg);
            	break;
            default:
            	
            }
            
            //Socket sckToClient = new Socket("localhost", )
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		if (args.length < 1) {
			System.out.println("Must specify port to listen on");
			System.out.println("e.g. 'PaxosNode 9001'");
			System.exit(1);
		}
		
		try {
			int port = Integer.parseInt(args[0]);
			PaxosNode server = new PaxosNode(port);
			
		} catch (Exception e) {
			System.out.println("Unable to parse port: "+args[0]);
			System.exit(1);
		}
		
		
	}
}
