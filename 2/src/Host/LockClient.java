package Host;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

import data.LockAction;
import data.Proj2Message;

/**
 * The core logic running a client for the proj 2 lock service
 *
 */
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

	/**
	 * Start receiving commands from the command line and sending requests out
	 * to the paxos-based lock service
	 * @throws IOException
	 */
	public void run() throws IOException {

		// open a listening socket
		msgSocket = new DatagramSocket(port);

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String command = null;
		System.out.println("Client started!");
		while (true) {
			clock ++;
			// receive the command from the command line
			System.out.print("Command: ");
			command = br.readLine();

			String[] commandSplit = command.split("\\s+");
			if (command == null || command.length() == 0) {
				System.out.println("Please enter a command. Eg. lock a");
			} else if (EXIT.equalsIgnoreCase(command)) {
				// if the command is to exit, exit!
				break;
			} else if (LOCK.equalsIgnoreCase(commandSplit[0])) {
				// send a lock request
				if (commandSplit.length < 2) {
					// need to specify what to lock on which port
					System.out.println("Must specify which lock to lock, e.g. 'lock a'");
				} else {
					// construct and send the lock request
					LockAction action = new LockAction();
					action.lock = true;
					action.client = port;
					action.lockName = commandSplit[1];
					action.uid = clock;

					Proj2Message req = new Proj2Message();
					req.clockVal = clock;
					req.from = port;
					req.command = Proj2Message.Command.LOCK_SERVICE_REQUEST;
					req.data = action;
					int destPort = PAXOS_MEMBERS[0];
					try{
						if (commandSplit.length > 2){
							destPort = Integer.parseInt(commandSplit[2]);
							if(destPort < 9002 || destPort > 9006){
								System.out.println("dest port number should lay "
										+ "between 9002 and 9006");
								continue;
							}

						}
					} catch (NumberFormatException e){
						System.out.println("Specified port is not a number");
						continue;
					}
					
					System.out.println("Sending lock request for "+commandSplit[1]);
					sendMessage(req, destPort);

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
					action.uid = clock;

					Proj2Message req = new Proj2Message();
					req.clockVal = clock;
					req.from = port;
					req.command = Proj2Message.Command.LOCK_SERVICE_REQUEST;
					req.data = action;
					int destPort = PAXOS_MEMBERS[0];
					try{
						if (commandSplit.length > 2){
							destPort = Integer.parseInt(commandSplit[2]);
							if(destPort < 9002 || destPort > 9006){
								System.out.println("dest port number should lay "
										+ "between 9002 and 9006");
								continue;
							}
						}
					} catch (NumberFormatException e){
						System.out.println("Specified server port is not a number");
						continue;
					}
					System.out.println("Sending unlock request for "+commandSplit[1]);
					sendMessage(req, destPort);
					receiveConfirmation(action);
				}
			} else {
				System.out.println("Unknown command: " + commandSplit[0]);
			}
		}

		msgSocket.close();
	}

	/**
	 * Block, receiving messages from the lock service until we receive a response
	 * to the specified request.
	 * @param actionToWaitFor
	 * @throws IOException
	 */
	private void receiveConfirmation(LockAction actionToWaitFor) throws IOException {
		Proj2Message msg = null;
		do {
			msg = receiveMessage();
		} while (!actionToWaitFor.equals(msg.data));
		LockAction la = (LockAction)msg.data;
		System.out.println(la.lockName + " is " + (la.lock ? "locked" : "unlocked"));
	}
}
