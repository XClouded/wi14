package sapphire.appexamples.jeffsdocs.app;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sapphire.app.SapphireObject;
import sapphire.appexamples.jeffsdocs.app.Data.Permissions;
import sapphire.appexamples.jeffsdocs.app.Doc.Position;

public class Sheet extends Data{
	private String[][] cells;				//text in the document
	private Map<String,Location> cursors;	//location of each user's cursor in the document
	
	/**
	 * Create a new spreadsheet named name
	 */
	public Sheet(User owner, String name) {
		super(owner,name,Permissions.BLOCKED);
		this.cells = new String[5][5];
		this.cursors = new HashMap<String,Location>();
	}
	
	/**
	 * Put text in currently selected cell by source
	 */
	public void insert(User source, String text) {
		if(!hasAccess(source, Permissions.WRITE))
			throw new IllegalArgumentException("Insufficient privledges");
		
		if(!cursors.containsKey(source.toString()))
			cursors.put(source.toString(), new Location(0,0));
		
		Location loc = cursors.get(source.toString());
		cells[loc.x][loc.y] = text;
	}
	
	/**
	 * Insert a row at location (and move all further rows down)
	 */
	public void insertRow(User source, int location) {
		if(!hasAccess(source, Permissions.WRITE))
			throw new IllegalArgumentException("Insufficient privledges");
		
		String[][] temp = new String[cells.length+1][cells[0].length];
		
		//copy over data
		for(int i = 0; i < cells.length; i++) {
			int offset = i > location ? 1 : 0;
			System.arraycopy(cells[i],0,temp[i+offset],0,cells[i].length);
		}
		
		//update locations
		for(String u : cursors.keySet()) {
			Location loc = cursors.get(u.toString());
			if(loc.x > location)
				loc.x++;
		}
		
		cells = temp;
	}
	
	/**
	 * Insert a column at location (and move all further columns to the left
	 */
	public void inserCol(User source, int location) {
		if(!hasAccess(source, Permissions.WRITE))
			throw new IllegalArgumentException("Insufficient privledges");
		
		String[][] temp = new String[cells.length][cells[0].length+1];
		
		//copy over data
		for(int i = 0; i < cells.length; i++) {
			System.arraycopy(cells[i], 0, temp[i], 0, location);
			System.arraycopy(cells[i], location, temp[i], location+1, cells.length - location);
		}
		
		//update locations
		for(String u : cursors.keySet()) {
			Location loc = cursors.get(u.toString());
			if(loc.y > location)
				loc.y++;
		}
		
		cells = temp;
	}

	/**
	 * Moves the specified user's cursor to specified location (or closest location in document)
	 */
	public void move(User source, int x, int y) {
		if(!hasAccess(source, Permissions.WRITE))
			throw new IllegalArgumentException("Insufficient privledges");
		
		if(!cursors.containsKey(source.toString()))
			cursors.put(source.toString(), new Location(0,0));
		
		Location at = cursors.get(source.toString());
		at.x = x;
		at.y = y;
	}
	
	/**
	 * Gets the text of this Sheet
	 */
	public String[][] read(User source) {
		if(!hasAccess(source, Permissions.READ))
			throw new IllegalArgumentException("Insufficient privledges");
		
		return cells;
	}
	
	/**
	 * Gets all the users currently editing this
	 */
	public Set<String> getUsers(User source) {
		if(!hasAccess(source, Permissions.READ))
			throw new IllegalArgumentException("Insufficient privledges");
		
		return cursors.keySet();
	}
	
	/**
	 * Gets the specified user's cursor location
	 */
	public Map<String,Location> getCursors(User source) {
		if(!hasAccess(source, Permissions.READ))
			throw new IllegalArgumentException("Insufficient privledges");
		return new HashMap<String,Location>(cursors);
	}
	
	public String toString() {
		return "SpreadSheet: " + name;
	}
	
	@Override
	public String type() {
		return "Sheet";
	}
	
	/**
	 * Helper class to track the user's selected boxes
	 */
	public class Location {
		public int x,y;
		public Location(int x,int y) {
			this.x = x; this.y = y;
		}
	}
	
	
	
	public void initialize(Data d) {super.initialize(d);}
	public boolean canShareData(User source, UserManager um, User other, Permissions p) {return super.canShareData(source, other, p);}
	public boolean hasAccess(User source, Permissions p) {return super.hasAccess(source, p);}
	public Permissions getLevel(User u) { return super.getLevel(u); }
	public void setPermissions(User o, Permissions p) { super.setPermissions(o, p); }
}
