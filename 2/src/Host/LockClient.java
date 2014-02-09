package Host;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

import data.LockAction;
import data.Proj2Message;


public class LockClient extends Proj2Node{
	private static final String EXIT = "exit";
	private static final String LOCK = "lock";
	private static final String UNLOCK = "unlock";

	private int clock, port;

	public LockClient(int port) throws UnknownHostException, IOException {
		super();

		clock = 0;
		this.port = port;
	}

	public void run() throws IOException {

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

					receiveConfirmation(action);
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
					receiveConfirmation(action);
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
	        req.command = Proj2Message.Command.LOCK_SERVICE_REQUEST;

			sendMessage(req, 9002);

			// wait for a response from a learner
			Proj2Message msg = receiveMessage();
			System.out.println("Got message as client: " + msg);*/
		}

		msgSocket.close();
	}

	private void receiveConfirmation(LockAction actionToWaitFor) throws IOException {
		Proj2Message msg = null;
		do {
			msg = receiveMessage();
			System.out.println("Got message as client: " + msg);
		} while (!actionToWaitFor.equals(msg.data));
	}
}
