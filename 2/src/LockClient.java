import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class LockClient {
	private static final String EXIT = "exit";
	public static void main(String[] args) throws UnknownHostException, IOException {
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
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		String command = null;
		System.out.println("Client started!");
		while(true) {
			// TODO command receive loop
			command = br.readLine();
			
			if(EXIT.equalsIgnoreCase(command)) break;
			
			Socket clientSocket = new Socket("localhost", 9001);
			ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
			
			outToServer.writeInt(5);			
			
			outToServer.close();
			clientSocket.close();
		}
	}
}
