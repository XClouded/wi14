package sapphire.appexamples.jeffsdocs.app;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sapphire.app.AppObjectNotCreatedException;
import sapphire.app.SapphireObject;
import sapphire.policy.replicate.PaxosReplicatePolicy;
import static sapphire.runtime.Sapphire.*;

public class User implements SapphireObject{//<PaxosReplicatePolicy> {//<TestPolicy>{//
	private String name;		//username
	private byte[] password;	//password
	private Set<Data> documents;	//collection of documents the user has
	private Set<Data> offeredDocs;	//collection of documents people wish to share with you
	private User thisUser;
	
	/**
	 * Create a new User with the specified username
	 * @param name
	 */
	public User(String name, String password) {
		this.name = name;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			this.password = md.digest(password.getBytes("UTF-8"));
		} catch(Exception ex) {ex.printStackTrace();}
		this.documents = new HashSet<Data>();
		this.offeredDocs = new HashSet<Data>();
	}
	
	public void initialize(User u) {
		thisUser = u;
	}

	/**
	 * Creates a new document with the specified name
	 * @return the new document
	 */
	public Doc newDoc(String name) {
		Doc d = null;
		d = (Doc) new_(Doc.class, thisUser, name);
		d.initialize(d);
		documents.add(d);
		System.out.println("Created new doc" + d.toString() + " for user " + thisUser.toString());
		return d;
	}
	
	/**
	 * Creates a new spreadsheet with the specified name
	 * @return the new spreadsheet
	 */
	public Sheet newSheet(String name) {
		Sheet s = null;
		s = (Sheet) new_(Sheet.class, thisUser, name);
		documents.add(s);
		System.out.println("Created new sheet" + s.toString() + " for user " + this.toString());
		return s;
	}
	
	/**
	 * Deletes doc from this user (if other user's still have a copy, they will keep theirs)
	 * @return status message
	 */
	public void deleteData(Data d) {
		documents.remove(d);
	}
	
	/**
	 * Add the given document to your set of documents
	 */
	public void addData(Data d) {
		System.out.println("adding data " + d + " to " + toString());
		offeredDocs.add(d);
	}
	
	/**
	 * Get all the documents belonging to this user
	 */
	public Set<Data> getData() {
		return new HashSet<Data>(documents);
	}
	
	public Set<Data> getOfferedData() {
		return new HashSet<Data>(offeredDocs);
	}
	
	public void acceptData(Data d) {
		offeredDocs.remove(d);
		documents.add(d);
	}
	
	/**
	 * Check whether a given password is correct
	 * @param MD5 hash of password
	 * @return true if password is correct
	 */
	public boolean checkPassword(String pass) {
		byte[] p = null;
		System.out.println("checking password: " + pass);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			p = md.digest(pass.getBytes("UTF-8"));
		} catch(Exception ex) {ex.printStackTrace();}
		
		if (p.length != password.length)
			return false;
		System.out.println("same length " + p.length);
		for (int i = 0; i < password.length; i++) {
			int loperand = (password[i] & 0xff);
			int roperand = (p[i] & 0xff);
			System.out.println("checking byte " + i);
			if (loperand != roperand) {
				return false;
			}
		}
		return true;
	}
	
	public String toString() {
		return name;
	}

}
