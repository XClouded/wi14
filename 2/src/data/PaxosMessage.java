package data;

import java.io.Serializable;

/**
 * Messages passed as data between paxos nodes
 *
 */
public class PaxosMessage implements Serializable{
	
	private static final long serialVersionUID = 8947344733198515849L;
	
	public int paxosRound;// paxos round number
	public int proposalNum;
	public LockAction value; //NULL if no value
	
	public PaxosMessage(int paxosRound, int proposalNum){
		this(paxosRound, proposalNum, null);
	}

	public PaxosMessage(int paxosRound, int proposalNum, LockAction value) {
		this.paxosRound = paxosRound;
		this.proposalNum = proposalNum;
		this.value = value;
	}
	
	public int hashCode(){
		return paxosRound + proposalNum + (value != null? value.hashCode() : 0);
	}
	
	public boolean equals(Object o){
		return o instanceof PaxosMessage 
				&& value.equals(((PaxosMessage)o).value);
	}

}
