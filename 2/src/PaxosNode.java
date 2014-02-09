import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;

import data.Proj2Message;

import state.PaxosState;


/**
 * Represents a node in a distributed paxos system. Designed to be the backbone of a distributed lock service.
 *
 */
public class PaxosNode {
	private static final int[] PAXOS_MEMBERS = {9002, 9003, 9004, 9005, 9006};
	
	// make the private variables static, should be one PaxosNode per process
	private static Map<Integer, PaxosState> roundState;
	private static int currentRound, clock, nid; 
	private static ServerSocket welcomeSocket;
	
	public static void main(String[] args) throws IOException {
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
		
		welcomeSocket = new ServerSocket(nid);
		
		System.out.println("PaxosNode started! Listening on port "+nid);
		
		// keep receiving messages
		while(true){
			Proj2Message msg = receiveMessage();
            
            // increment the local clock
            clock = Math.max(clock, msg.clockVal) + 1;
            
            //TODO handle each request based on the command
            switch(msg.command) {
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
	
	public static Proj2Message receiveMessage() throws IOException {
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
        
        return msg;
	}
	
	public static void sendMessage(Proj2Message msg, int to) throws UnknownHostException, IOException {
		Socket sckToClient = new Socket("localhost", to);
        ObjectOutputStream outToClient = new ObjectOutputStream(sckToClient.getOutputStream());
        
        outToClient.writeObject(msg);
        
        outToClient.close();
        sckToClient.close();
	}
	
	public static void sendToAllOtherPaxos(Proj2Message msg) throws UnknownHostException, IOException {
		for(int node : PAXOS_MEMBERS) {
			if (node != nid) {
				try {
					sendMessage(msg, node);
				} catch (ConnectException e) {
					// could not connect
					// node is down, ignore
				}
			}
		}
	}
}
