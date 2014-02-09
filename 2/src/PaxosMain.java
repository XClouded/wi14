import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import Host.PaxosNode;


public class PaxosMain {
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("Must specify port to listen on");
			System.out.println("e.g. 'PaxosMain 9001'");
			System.exit(1);
		}
		
		PaxosNode node = new PaxosNode(args);
		
		node.run();
	}
}
