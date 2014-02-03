import java.util.Map;


/**
 * Holds the paxos state machine. Designed to be the backbone of a distributed lock service.
 * @author JakeS
 *
 */
public class PaxosNode {
	Map<Integer, Integer> roundState; //TODO make this real
	int currentRound;
	int clock;
	
	public PaxosNode () {
		clock = 0;
		currentRound = 0;
	}
}
