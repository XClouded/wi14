package Host;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import data.Proj2Message;


public class LockClient {
	private static final int[] PAXOS_MEMBERS = {9002, 9003, 9004, 9005, 9006};
	private static final String EXIT = "exit";
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		if (args.length < 1) {
			System.out.println("Must specify port to listen on");
			System.out.println("e.g. 'LockClient 9000'");
			System.exit(1);
		}
		
		int port = 0;
		
		try {
			port = Integer.parseInt(args[0]);
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
			
			// send a dumb int to the paxos node at 9001
			Socket outSocket = new Socket("localhost", 9001);
			ObjectOutputStream outToServer = new ObjectOutputStream(outSocket.getOutputStream());
			
			outToServer.writeInt(5);			
			
			// close the to server connection
			outToServer.close();
			outSocket.close();
			
			// wait for a response from a learner
			ServerSocket listenSocket = new ServerSocket(port);
			Socket responseSocket = listenSocket.accept();
			
			// read in the message
			ObjectInputStream inFromServer = new ObjectInputStream(responseSocket.getInputStream());
			Proj2Message msg = null;
			try {
				msg = (Proj2Message)inFromServer.readObject();
			} catch (ClassNotFoundException e) {
				System.err.println("Class not found exception, failed to cast message");
			}
			
			//TODO keep working on message processing
			// close the from server connection
			responseSocket.close();
			listenSocket.close();
		}
	}
}
