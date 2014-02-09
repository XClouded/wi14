package data;

import java.io.Serializable;

/**
 * Class to pass messages around.
 */
public class Proj2Message implements Serializable{
	private static final long serialVersionUID = 1L;

	public enum Command {
		// paxos commands
		PREPARE, PROMISE, ACCEPT_REQUEST, ACCEPTED, LEARN, 
		
		// lock service commands
		LOCK_REQUEST, LOCK_RESPONSE
	}
	
	public int clockVal;
	public Command command;
	public Serializable data;
	
	public String toString() {
		String str = "clock: " + clockVal;
		str += " command: " + command;
		str += " data: " + data;
		
		return str;
	}
	
}
