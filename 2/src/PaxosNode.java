import java.util.Map;

import state.PaxosState;


/**
 * Represents a node in a distributed paxos system. Designed to be the backbone of a distributed lock service.
 *
 */
public class PaxosNode {
	Map<Integer, PaxosState> roundState;
	int currentRound; // for multi-paxos
	int clock; // for proposal #'s
	
	public PaxosNode () {
		clock = 0;
		currentRound = 0;
	}
}
