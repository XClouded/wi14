package data;

import java.io.Serializable;

/**
 * Class to pass messages around. 
 * This is used both for client-server communication as well as inter-paxos communication.
 */
public class Proj2Message implements Serializable{
	private static final long serialVersionUID = 1L;

	public enum Command {
		// paxos commands
		PREPARE, PROMISE, ACCEPT_REQUEST, ACCEPTED, LEARN, 
		
		// lock service commands
		LOCK_SERVICE_REQUEST, LOCK_SERVICE_RESPONSE
	}
	
	public int clockVal, from;
	public int to; // -1 means broadcast 
	public Command command;
	public Serializable data;
	
	public String toString() {
		String str = "from node: " + from;
		str += " clock: " + clockVal;
		str += " command: " + command;
		str += " data: " + data;
		
		return str;
	}
	
}
