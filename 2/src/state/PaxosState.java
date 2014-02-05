package state;

import java.io.Serializable;

/**
 * Represents the states of a proposer, acceptor, and learner for a single instance of paxos.
 *
 */
public class PaxosState implements Serializable{
	public Proposer proposer;
	public Acceptor acceptor;
	public Learner learner;
	
	public void PaxosState() {
		proposer = new Proposer();
		acceptor = new Acceptor();
		learner = new Learner();
	}
}
