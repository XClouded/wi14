package sapphire.appexamples.jeffsdocs.app;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import sapphire.app.AppObjectNotCreatedException;
import sapphire.app.SapphireObject;
import sapphire.appexamples.jeffsdocs.app.Data.Permissions;
import sapphire.oms.OMSServer;
import sapphire.policy.dht.DHTPolicy;
import sapphire.policy.interfaces.dht.DHTInterface;
import sapphire.policy.interfaces.dht.DHTKey;
import static sapphire.runtime.Sapphire.*;

/**
 * A class that acts as the access point for the application, allowing access users that one isn't logged
 * in as. Uses a distributed hash table to support access to many users.
 * 
 * @author ackeri
 *
 */
public class UserManager implements SapphireObject<DHTPolicy>, DHTInterface {
    Map<DHTKey, User> users = new Hashtable<DHTKey, User>();

	public User createUser(String name,String password) {
		System.out.println("Creating User: " + name);
		User u = null;
		u = (User) new_(User.class, name, password);
		u.initialize(u);
		users.put(new DHTKey(name), u);
		return u;
	}
	
	public User login(String name, String password) {
		User u = users.get(new DHTKey(name));
		if(u.checkPassword(password))
			return u;
		return null;

	}

	//@Override
	public Map<DHTKey, ?> dhtGetData() {
		return users;
	}

	public void shareData(String other, User source, Data d, Permissions p) {
		User o = users.get(new DHTKey(other));
		if(!d.canShareData(source, o, p))
			throw new IllegalArgumentException("Cannot Share data");
		
		//need to give permission in the document
		d.setPermissions(o,p);
			
		if(p.ordinal() >= Permissions.READ.ordinal())
			o.addData(d);
	}
}
