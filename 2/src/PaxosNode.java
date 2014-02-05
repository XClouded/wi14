import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Map;

import state.PaxosState;


/**
 * Represents a node in a distributed paxos system. Designed to be the backbone of a distributed lock service.
 *
 */
public class PaxosNode {	
	public static void main(String[] args) throws SocketException {
		Map<Integer, PaxosState> roundState;
		int currentRound; // for multi-paxos
		int clock; // for proposal #'s
		
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
		
		DatagramSocket serverSocket = new DatagramSocket(listenPort);
		
		// TODO set up a receive loop
	}
}
