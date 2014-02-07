package data;

import java.io.Serializable;

public class LockAction implements Serializable{
	public boolean lock;
	public String lockName;
	public int client;
}
