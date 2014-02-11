import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import Host.PaxosNode;

/**
 * A single paxos/server node in the lock service.
 * 
 * Takes in a port number as an argument to send/receive messages on.
 */
public class PaxosMain {
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("Must specify port to listen on");
			System.out.println("e.g. 'PaxosMain 9001'");
			System.exit(1);
		}
		
		int nodeId = 0;
		
		try {
			nodeId = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Unable to parse port: "+args[0]);
			System.exit(1);
		}
		
		PaxosNode node = new PaxosNode(nodeId);
		
		node.run();
	}
}
