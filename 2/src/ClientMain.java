import java.io.IOException;
import java.net.UnknownHostException;

import Host.LockClient;


public class ClientMain {
	public static void main(String[] args) throws UnknownHostException, IOException {
		LockClient client = new LockClient(args);
		
		client.run();
	}
}
