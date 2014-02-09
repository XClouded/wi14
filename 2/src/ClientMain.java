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
		
		LockClient client = new LockClient(args);
		
		client.run();
	}
}
