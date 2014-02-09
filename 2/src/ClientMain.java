import java.io.IOException;
import java.net.UnknownHostException;

import Host.LockClient;


public class ClientMain {
	public static void main(String[] args) throws UnknownHostException, IOException {
		if (args.length < 1) {
			System.out.println("Must specify port to listen on");
			System.out.println("e.g. 'ClientMain 9000'");
			System.exit(1);
		}
		
		int port = 0;
		
		try {
			port = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Unable to parse port: "+args[0]);
			System.exit(1);
		}
		
		LockClient client = new LockClient(port);
		
		client.run();
	}
}
