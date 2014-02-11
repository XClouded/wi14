package data;

import java.io.Serializable;

/**
 * Represents a single action to be taken by the lock service.
 */
public class LockAction implements Serializable{
	private static final long serialVersionUID = 1L;
	public boolean lock; // lock or unlock
	public String lockName;
	public int client;
	public int uid;
	
	public int hashCode(){
		return lockName.hashCode() + client + (lock ? 1 : 0) + uid;
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
				|| lockName.equals(other.lockName))
				&& uid == other.uid;

		return equal;
	}
	
	public String toString(){
		return "" + client + " wants to " + (lock ? "lock" : "unlock") + " " + lockName + " uid: " + uid;
	}
}
