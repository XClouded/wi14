import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import Host.PaxosNode;


public class PaxosMain {
	public static void main(String[] args) throws IOException {
		PaxosNode node = new PaxosNode(args);
		
		node.run();
	}
}
