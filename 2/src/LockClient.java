import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
	private static DatagramSocket msgSocket;
	private static InetAddress IPAddress;

	public static void main(String[] args) throws UnknownHostException, IOException {
		if (args.length < 1) {
			System.out.println("Must specify port to listen on");
			System.out.println("e.g. 'LockClient 9000'");
			System.exit(1);
		}

		clock = 0;
		port = 0;
		IPAddress = InetAddress.getByName("localhost");

		try {
			port = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Unable to parse port: "+args[0]);
			System.exit(1);
		}

		// open a listening socket
		msgSocket = new DatagramSocket(port);

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

			
			// send a test message to the paxos node at 9002
			// TODO remove test code
	        Proj2Message req = new Proj2Message();
	        req.clockVal = clock;
	        req.from = port;
	        req.command = Proj2Message.Command.LOCK_SERVICE_REQUEST;

			sendMessage(req, 9002);

			// wait for a response from a learner
			Proj2Message msg = receiveMessage();
			System.out.println("Got message as client: " + msg);
		}
		
		msgSocket.close();
	}

	/**
	 * Receive a Proj2Message on msgSocket
	 * @return the received Proj2Message
	 * @throws IOException
	 */
	private static Proj2Message receiveMessage() throws IOException {
		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		msgSocket.receive(receivePacket);

		Proj2Message msg = null;
		ByteArrayInputStream bis = new ByteArrayInputStream(receiveData);
		ObjectInput in = null;
		try {
			// convert the bytes back to a msg
			in = new ObjectInputStream(bis);
			Object o = in.readObject();
			msg = (Proj2Message)o;
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found exception, failed to cast message");
		} finally {
			try {
				bis.close();
			} catch (IOException ex) {
				// ignore close exception
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
		}

		return msg;
	}

	/**
	 * Send a Proj2Message to the specified node on msgSocket
	 * @param msg the Proj2Message to send
	 * @param to the node to send it to
	 * @throws IOException
	 */
	private static void sendMessage(Proj2Message msg, int to) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			// convert msg to bytes
			out = new ObjectOutputStream(bos);   
			out.writeObject(msg);
			byte[] bytes = bos.toByteArray();

			// send the message
			DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, IPAddress, to);
			msgSocket.send(sendPacket);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
			try {
				bos.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}
	}
}
