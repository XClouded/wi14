package data;

import java.io.Serializable;

public class PaxosMessage implements Serializable{
	public enum PaxCommand {
		PREPARE, PROMISE, ACCEPT_REQUEST, ACCEPTED, LEARN
	}
	
	public int clockVal;
	public PaxCommand command;
	
}
