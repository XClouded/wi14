package data;

import java.io.Serializable;

public class PaxosMessage implements Serializable{
	
	private static final long serialVersionUID = 8947344733198515849L;
	
	public int proposalNum;
	public LockAction value;

	public PaxosMessage(int proposalNum, LockAction value) {
		this.proposalNum = proposalNum;
		this.value = value;
	}

}
