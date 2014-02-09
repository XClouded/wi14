package state;

import java.io.Serializable;

import data.LockAction;
import data.Proj2Message;

public class Learner implements Serializable{
	private static final long serialVersionUID = 1L;
	public LockAction learnedValue;

	public Learner(){
		
	}
	
	public Proj2Message handleMessage(Proj2Message msg){
		
		return null;
	}
}
