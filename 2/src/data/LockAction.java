package data;

import java.io.Serializable;

public class LockAction implements Serializable{
	private static final long serialVersionUID = 1L;
	public boolean lock;
	public String lockName;
	public int client;
}
