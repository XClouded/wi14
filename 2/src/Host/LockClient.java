package Host;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import data.LockAction;
import data.Proj2Message;


public class LockClient {
	private static final int[] PAXOS_MEMBERS = {9002, 9003, 9004, 9005, 9006};
	private static final String EXIT = "exit";
	private static final String LOCK = "lock";
	private static final String UNLOCK = "unlock";
	
	// static assumes 1 lock client per process
	private static int clock, port;
	private static ServerSocket listenSocket;
	
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
		
		// open a listening socket
		listenSocket = new ServerSocket(port);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		String command = null;
		System.out.println("Client started!");
		while (true) {
			// receive the command from the command line
			System.out.print("Command: ");
			command = br.readLine();
			
			String[] commandSplit = command.split("\\s+");
			if (command == null || command.length() == 0) {
				System.out.println("Please enter a command.");
			} else if (EXIT.equalsIgnoreCase(command)) {
				// if the command is to exit, exit!
				break;
			} else if (LOCK.equalsIgnoreCase(commandSplit[0])) {
				// send a lock request
				if (commandSplit.length < 2) {
					// need to specify what to lock!
					System.out.println("Must specify which lock to lock, e.g. 'lock a'");
				} else {
					// construct and send the lock request
					LockAction action = new LockAction();
			        action.lock = true;
			        action.client = port;
			        action.lockName = commandSplit[1];
			        
			        Proj2Message req = new Proj2Message();
			        req.clockVal = clock;
			        req.from = port;
			        req.command = Proj2Message.Command.LOCK_SERVICE_REQUEST;
			        req.data = action;
			        
			        System.out.println("Sending unlock request for "+commandSplit[1]);
			        sendMessage(req, PAXOS_MEMBERS[0]);
			        Proj2Message resp = receiveMessage();
			        //TODO debug print
			        System.out.println("lock requst response fomr server: "+resp);
				}
			} else if (UNLOCK.equalsIgnoreCase(commandSplit[0])) {
				// send an unlock request
				if (commandSplit.length < 2) {
					System.out.println("Must specify which lock to unlock, e.g. 'unlock a'");
				} else {
					LockAction action = new LockAction();
			        action.lock = false;
			        action.client = port;
			        action.lockName = commandSplit[1];
			        
			        Proj2Message req = new Proj2Message();
			        req.clockVal = clock;
			        req.from = port;
			        req.command = Proj2Message.Command.LOCK_SERVICE_REQUEST;
			        req.data = action;
			        
			        System.out.println("Sending unlock request for "+commandSplit[1]);
			        sendMessage(req, PAXOS_MEMBERS[0]);
			        Proj2Message resp = receiveMessage();
			        //TODO debug print
			        System.out.println("lock requst response fomr server: "+resp);
				}
			} else {
				System.out.println("Unknown command: " + commandSplit[0]);
			}
			
			/*
			// send a test message to the paxos node at 9002
			// TODO remove test code
	        Proj2Message req = new Proj2Message();
	        req.clockVal = clock;
	        req.from = port;
	        req.command = Proj2Message.Command.LOCK_REQUEST;
	        
			sendMessage(req, 9002);
			
			// wait for a response from a learner
			Proj2Message msg = receiveMessage();
			System.out.println("Got message as client: " + msg);*/
		}
	}
	
	private static Proj2Message receiveMessage() throws IOException {
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
