package sapphire.appexamples.jeffsdocs.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sapphire.app.*;
import sapphire.appexamples.jeffsdocs.app.Data.Permissions;
import sapphire.policy.cache.CacheLeasePolicy;
import sapphire.policy.replicate.PaxosReplicatePolicy;

/**
 * A collaboratively editable text document. Assumes atomic method calls to provide correct
 * cursor response to others edits.
 * 
 * @author ackeri
 *
 */
public class Doc extends Data implements SapphireObject{//<PaxosReplicatePolicy> {
	private StringBuilder body;			//text in the document
	private Map<String,Position> cursors;	//location of each user's cursor in the document

	/**
	 * Creates a new named document
	 */
	public Doc(User owner,String name) {
		super(owner,name,Permissions.BLOCKED);
		this.body = new StringBuilder();
		this.cursors = new HashMap<String,Position>();
	}

	/**
	 * Inserts specified text as specified user (using their position in the document). To 
	 * delete text, insert backspace characters (this feature is slow).
	 */
	public void insert(User source, String text) {
		if(!hasAccess(source, Permissions.WRITE))
			throw new IllegalArgumentException("Insufficient privledges for " + source.toString() + " writing to " + this + " was " + this.getLevel(source));
		
		if(text == null)
			text = "";
		if(!cursors.containsKey(source.toString()))
			cursors.put(source.toString(), new Position(0));
		
		//perform operation
		Position at = cursors.get(source.toString());
		if(at.start != at.end && text.startsWith("\b"))	//copying behaviour of most editors
			text = text.substring(1);
		body.replace(at.start, at.end, text);
		
		//handle the insertion of backspace characters
		int count = 0;
		if(text.indexOf('\b') != -1) {
			String temp = body.toString();
			while(temp.indexOf('\b') != -1) {
				temp = temp.replaceFirst(".[\b]", "");
				count += 2;
			}
			body = new StringBuilder(temp);
		}
		
		//move cursors (assumes backspaces happen first for the purposes of moving cursors)
		updateUsers(at.start-count, text.length() - at.length() - count);
		at.start += text.length() - at.length() - count;
		at.end = at.start;
	}

	/**
	 * Moves the specified user's cursor to specified location (or closest location in document)
	 */
	public void move(User source, int loc) {
		if(!hasAccess(source, Permissions.WRITE))
			throw new IllegalArgumentException("Insufficient privledges");

		if(!cursors.containsKey(source.toString()))
			cursors.put(source.toString(), new Position(0));
		
		Position at = cursors.get(source.toString());
		
		int temp = boundPos(loc);
		at.start = temp;
		at.end = temp;
		System.out.println("moving as " + source + " to " +at);
	}
	
	/**
	 * Highlights the range [start,end) as specified user
	 */
	public void highlight(User source, int start, int end) {
		if(!hasAccess(source, Permissions.WRITE))
			throw new IllegalArgumentException("Insufficient privledges");

		if(!cursors.containsKey(source.toString()))
			cursors.put(source.toString(), new Position(0));
		
		Position at = cursors.get(source.toString());
		
		at.start = boundPos(start);
		at.end = boundPos(end);
		at.end = Math.max(at.start, at.end);
		System.out.println("highlighting as " + source + " over " + at);
	}
	
	/**
	 * Gets the text of this document
	 */
	public String read(User source) {
		if(!hasAccess(source, Permissions.READ))
			throw new IllegalArgumentException("Insufficient privledges");
		return body.toString();
	}
	
	/**
	 * Gets all the users currently editing this
	 */
	public Map<String,Permissions> getUsers(User source) {
		if(!hasAccess(source, Permissions.READ))
			throw new IllegalArgumentException("Insufficient privledges");
		return new HashMap<String,Permissions>(users);
	}
	
	/**
	 * Gets the specified user's cursor location
	 */
	public Map<String,Position> getCursors(User source) {
		if(!hasAccess(source, Permissions.READ))
			throw new IllegalArgumentException("Insufficient privledges");
		return new HashMap<String,Position>(cursors);
	}
	
	public String toString() {
		return "Document: " + name;
	}
	
	/**
	 * little helper to find the closest location actually in the document
	 */
	private int boundPos(int pos) {
		if(pos < 0)
			return 0;
		else if(pos > body.length())
			return body.length();
		return pos;
	}
	
	/**
	 * helper to push people's cursors around if other people are typing
	 * @param at		location of change in text
	 * @param amount	amount of characters added (negative if deleted)
	 */
	private void updateUsers(int at, int amount) {
		for(String u : cursors.keySet()) {
			Position loc = cursors.get(u);
			if(loc.start > at) {
				loc.start += amount;
			}
			if(loc.end > at) {
				loc.end += amount;
			}
			loc.start = boundPos(loc.start);
			loc.end = boundPos(loc.end);
		}
	}
	
	/**
	 * A simple struct for representing a user's cursor (or possibly a selection). The start and end position
	 * represent a selection range [start,end).
	 */
	public class Position implements Serializable{
		public int start;
		public int end;
		
		public Position(int loc) {
			start = loc;
			end = loc;
		}
		
		public Position(int start, int end) {
			this.start = start;
			this.end = end;
		}
		
		public int length() {
			return end - start;
		}
		
		public String toString() {
			return "[" + start + "," + end +")";
		}
	}

	@Override
	public String type() {
		return "Doc";
	}
	
	//here so that the stub generator recognizes inherited methods
	public void initialize(Data d) {super.initialize(d);}
	public boolean canShareData(User source, UserManager um, User other, Permissions p) {return super.canShareData(source, other, p);}
	public boolean hasAccess(User source, Permissions p) {return super.hasAccess(source, p);}
	public Permissions getLevel(User u) { return super.getLevel(u); }
	public void setPermissions(User o, Permissions p) { super.setPermissions(o, p); }
}



