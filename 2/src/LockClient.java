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
	private static final String LOCK = "lock";
	private static final String UNLOCK = "unlock";
	
	// static assumes 1 lock client per process
	private static int clock, port;
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		if (args.length < 1) {
			System.out.println("Must specify port to listen on");
			System.out.println("e.g. 'LockClient 9000'");
			System.exit(1);
		}
		
		clock = 0;
		port = 0;
		
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
			// receive the command from the command line
			System.out.print("Command: ");
			command = br.readLine();
			
			// if the command is to exit, exit!
			if(EXIT.equalsIgnoreCase(command)) break;
			
			// send a test message to the paxos node at 9002
			// TODO remove test code
	        Proj2Message req = new Proj2Message();
	        req.clockVal = clock;
	        req.from = port;
	        req.command = Proj2Message.Command.LOCK_REQUEST;
	        
			sendMessage(req, 9002);
			
			// wait for a response from a learner
			Proj2Message msg = receiveMessage();
			System.out.println("Got message as client: " + msg);
		}
	}
	
	private static Proj2Message receiveMessage() throws IOException {
		ServerSocket listenSocket = new ServerSocket(port);
		Socket responseSocket = listenSocket.accept();
		
		ObjectInputStream inFromServer = new ObjectInputStream(responseSocket.getInputStream());
		Proj2Message msg = null;
		try {
			msg = (Proj2Message)inFromServer.readObject();
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found exception, failed to cast message");
		}
		
		// close the from server connection
		responseSocket.close();
		listenSocket.close();
		
		return msg;
	}
	
	private static void sendMessage(Proj2Message msg, int to) throws IOException {
		Socket outSocket = new Socket("localhost", to);
		ObjectOutputStream outToServer = new ObjectOutputStream(outSocket.getOutputStream());
        
        outToServer.writeObject(msg);
		
		// close the to server connection
		outToServer.close();
		outSocket.close();
	}
}
