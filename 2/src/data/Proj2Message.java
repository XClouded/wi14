package data;

import java.io.Serializable;
import java.util.Set;

/**
 * Class to pass messages around.
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
	public int[] to;
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
