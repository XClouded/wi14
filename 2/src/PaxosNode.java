import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import data.Proj2Message;

import state.PaxosState;


/**
 * Represents a node in a distributed paxos system. Designed to be the backbone of a distributed lock service.
 *
 */
public class PaxosNode {
	private static final int[] PAXOS_MEMBERS = {9002, 9003, 9004, 9005, 9006};
	
	public static void main(String[] args) throws IOException {
		Map<Integer, PaxosState> roundState;
		int currentRound = 0; // for multi-paxos
		int clock = 0; // for proposal #'s
		int nid = 0;
		
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
            switch(msg.command) {
            	default:
            }
            
            //Socket sckToClient = new Socket("localhost", )
		}
	}
}
