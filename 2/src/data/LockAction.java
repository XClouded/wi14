package data;

import java.io.Serializable;

public class LockAction implements Serializable{
	private static final long serialVersionUID = 1L;
	public boolean lock;
	public String lockName;
	public int client;
	
	public int hashCode(){
		return lockName.hashCode() + client + (lock ? 1 : 0);
	}
	
	public boolean equals(Object o) {
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		
		LockAction other = (LockAction)o;
		
		boolean equal = 
				lock == other.lock
				&& client == other.client
				&& ((lockName == null && other.lockName == null)
				|| lockName.equals(other.lockName));

		return equal;
	}
}
