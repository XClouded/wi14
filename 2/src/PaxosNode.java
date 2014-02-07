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
	public static void main(String[] args) throws IOException {
		Map<Integer, PaxosState> roundState;
		int currentRound = 0; // for multi-paxos
		int clock = 0; // for proposal #'s
		
		if (args.length < 1) {
			System.out.println("Must specify port to listen on");
			System.out.println("e.g. 'PaxosNode 9001'");
			System.exit(1);
		}
		
		int listenPort = 0;
		
		try {
			listenPort = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Unable to parse port: "+args[0]);
			System.exit(1);
		}
		
		ServerSocket welcomeSocket = new ServerSocket(listenPort);
		
		// TODO set up a receive loop
		while(true){
			Socket connectionSocket = welcomeSocket.accept();
			ObjectInputStream inFromClient = new ObjectInputStream(connectionSocket.getInputStream());
			
			try {
				Proj2Message msg = (Proj2Message)inFromClient.readObject();
				
				switch(msg.command) {
				//case 
				}
			} catch (ClassNotFoundException e) {
				System.err.println("Class not found exception, falied to cast message");
			}
			
            inFromClient.close();
            connectionSocket.close();
		}
	}
}
