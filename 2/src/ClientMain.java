import java.io.IOException;
import java.net.UnknownHostException;

import Host.LockClient;

/**
 * A single client node which makes use of the paxos lock service.
 * 
 * Takes in a port number as an argument to send/receive messages on.
 */
public class ClientMain {
	public static void main(String[] args) throws UnknownHostException, IOException {
		if (args.length < 1) {
			System.out.println("Must specify port to listen on");
			System.out.println("e.g. 'ClientMain 9000'");
			System.exit(1);
		}
		
		// parse the port
		int port = 0;
		
		try {
			port = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Unable to parse port: "+args[0]);
			System.exit(1);
		}
		
		// start a client
		LockClient client = new LockClient(port);
		
		client.run();
	}
}
