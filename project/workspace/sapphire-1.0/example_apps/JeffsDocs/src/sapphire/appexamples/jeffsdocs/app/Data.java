package sapphire.appexamples.jeffsdocs.app;

import java.util.HashMap;
import java.util.Map;

import sapphire.app.SapphireObject;
import sapphire.appexamples.jeffsdocs.app.Data.Permissions;
import sapphire.policy.replicate.PaxosReplicatePolicy;

/**
 * A class built as an interface for interaction with collaboratively edited data. Provides
 * some permissions handling on a user by user basis based off of a whitelist mechanic with
 * a background level for unknown users. There is a special elevated permission for the user
 * creating the document.
 * 
 * type() is the mechanic for distinguishing different subclasses of data. Subclasses should
 * override this method to return a String descriptor.
 * 
 * @author ackeri
 *
 */
public class Data implements SapphireObject{//<PaxosReplicatePolicy> {
	/**
	 * Permission states
	 * BANNED is for users that were banned by the owner (can only be removed by the owner)
	 * BLOCKED is for users banned by a sharer (can be removed by any sharer)
	 * READ is for users allowed to read the document, but not edit
	 * WRITE is for users allowed to read and write, but not share
	 * SHARE is for users who can read, write and share the document, as well as block other users
	 * OWNER is for the user who has full rights to the document
	 * @author ackeri
	 */
	public static enum Permissions {
		BANNED,BLOCKED,READ,WRITE,SHARE,OWNER
	}
	
	protected Data thisData;				//this pointer (because it isn't provided by sapphire
	protected String name;				//name of the document
	protected Map<String,Permissions> users;	//access rights of various users
	private Permissions background;
	
	public Data(User owner, String name, Permissions background) {
		this.name = name;
		this.users = new HashMap<String,Permissions>();
		users.put(owner.toString(), Permissions.OWNER);
		this.background = background;
	}

	public void initialize(Data d) {
		thisData = d;
	}
	
	public boolean canShareData(User source, User other, Permissions p) {
		
		Permissions s = getLevel(source);
		Permissions o = getLevel(other);
		
		if(o.equals(p))
			return true; //the source requested no change

		if(s.ordinal() < Permissions.SHARE.ordinal())
			return false;//throw new IllegalArgumentException("Insufficient permissions to share");
		
		if(s.ordinal() < p.ordinal())
			return false;//throw new IllegalArgumentException("Cannot give someone more permissions than yourself");
		
		if(s.ordinal() <= o.ordinal())
			return false;//throw new IllegalArgumentException("Cannot modify permissions of your equals or betters");
		
		if(o == Permissions.BANNED && users.get(source.toString()) != Permissions.OWNER)
			return false;//throw new IllegalArgumentException("Only the owner can unban users");
		
		return true;
	}
	
	public Permissions getLevel(User u) {
		Permissions ret = users.get(u.toString());
		if(ret == null)
			ret = background;
		return ret;
	}
	
	public boolean hasAccess(User source, Permissions p) {
		return getLevel(source).ordinal() >= p.ordinal();
	}

	public String type() {return null;}

	//TODO: this should not be a public method
	public void setPermissions(User o, Permissions p) {
		users.put(o.toString(),p);
	}
}
