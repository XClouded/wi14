package state;

import java.io.Serializable;

import Host.PaxosNode;

/**
 * Represents the states of a proposer, acceptor, and learner for a single instance of paxos.
 *
 */
public class PaxosState implements Serializable {
	private static final long serialVersionUID = 1L;
	public Proposer proposer;
	public Acceptor acceptor;
	public Learner learner;
	
	public PaxosState(PaxosNode server) {
		proposer = new Proposer(server);
		acceptor = new Acceptor();
		learner = new Learner();
	}
	
}
