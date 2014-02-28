package sapphire.appexamples.jeffsdocs.device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import android.app.Activity;

import sapphire.app.SapphireActivity;
import sapphire.app.SapphireObject;
import sapphire.appexamples.jeffsdocs.app.Data;
import sapphire.appexamples.jeffsdocs.app.Doc;
import sapphire.appexamples.jeffsdocs.app.Sheet;
import sapphire.appexamples.jeffsdocs.app.User;
import sapphire.appexamples.jeffsdocs.app.UserManager;
import sapphire.kernel.server.KernelServer;
import sapphire.kernel.server.KernelServerImpl;
import sapphire.oms.OMSServer;


public class DocsActivity implements SapphireActivity {
	
	/**
	 * A method for printing a String[][] like it is a spreadsheet
	 */
	private static String transform(String[][] cells) {
		StringBuilder ret = new StringBuilder();
		for(int i = 0; i < cells.length; i++) {
			for(int j = 0; j < cells[i].length; j++) {
				ret.append("\t" + cells[i][j]);
			}
			ret.append("\n");
		}
		return ret.toString();
	}

	@Override
	public void onCreate(SapphireObject arg0) {
		if(arg0 != null)
			return;
		UserManager um = (UserManager) arg0;
		
        System.out.println("Received um: " + um);
        
        //KernelServer nodeServer = new KernelServerImpl(null, null, null, null);
        
        //create all users first so that things can blindly be shared
        System.out.println("-----Creating Users-----");
        List<UserActivity> users = new ArrayList<UserActivity>();
        for(int i = 0; i < USER_NUM; i++) {
        	users.add(new UserActivity(um,i));
        }
        
        System.out.println("-----Starting Users-----");
        for(UserActivity u : users)
        	u.start();
	}
	
	/**
	 * A little test script to reason about
	 */
	public void onCreate2(SapphireObject arg0) {
		UserManager um = (UserManager) arg0;
		
        System.out.println("Received um: " + um);
        
        //KernelServer nodeServer = new KernelServerImpl(null, null, null, null);
        
        User u1 = um.createUser("Jeff","password");
        
        System.out.println("created user " + u1);
        
        User u2 = um.createUser("Hank","likestodo");
        System.out.println("created user " + u2);
        
        //Sheet s1 = u1.newSheet("testsheet");
        
        Doc d1 = u1.newDoc("grocery list");
        System.out.println("created doc " + d1 + " as " + u1);
        
        d1.insert(u1, "Carrots\nasdf");
        System.out.println("wrote carrots to document");
        
        um.shareData("Hank", u1, d1, Data.Permissions.WRITE);
        System.out.println("shared doc " + d1 + " with " + u2 + " which has contents:\n" + d1.read(u2));
        
        Set<Data> dset = u2.getOfferedData();
        if(dset.isEmpty())
        	System.out.println(u2 + " has no documents!");
        Iterator<Data> iter = dset.iterator();
        Doc d2 = (Doc)iter.next();	//we know it is a doc, but could check with .type()
        u2.acceptData(d2);
        if(u2.getData().isEmpty())
        	System.out.println("Hank could not accept document");
        d2.move(u2, d2.read(u2).length());
        d2.insert(u2,"\b\b\b\bOrangutangs\n");
        System.out.println("we now have a " + d1 + " with contents:\n" + d1.read(u1));
        
        //make several edits
        System.out.println("jeff writes oranges");
        d1.insert(u1, "Oranges\n");
        System.out.println("we now have a " + d1 + " with contents:\n" + d1.read(u1));
        
        System.out.println("hank highlights oranges");
        d2.highlight(u2, 20, 28);	//highlight oranges
        System.out.println("jeff writes Kittens");
        d1.insert(u1, "Kittens\n");
        System.out.println("we now have a " + d1 + " with contents:\n" + d1.read(u1));
        
        System.out.println("jeff moves cursor to 0");
        d1.move(u1, 0);
        System.out.println("jeff writes Apples");
        d1.insert(u1, "Apples\n");
        System.out.println("we now have a " + d1 + " with contents:\n" + d1.read(u1));
        
        System.out.println("hank writes nothing (hopefully deleting orages)");
        d2.insert(u2, "");
        System.out.println("we now have a " + d1 + " with contents:\n" + d1.read(u1));
        
        
        System.out.println("we now have a " + d1 + " with contents:\n" + d1.read(u1));
		
        User u3 = um.login("Hank", "likestodo");
        User u4 = um.login("Hank", "likestwitter");
        System.out.println("has " + u3 + " " + u4 + "(hopefully Hank null)");
	}
	
	static final int EVENTS_PER_USER = 10;
	static final int USER_NUM = 10;
	static final Random rand = new Random();

	//these probabilities are cumulative in the order they appear here
	static final double EDIT_CHANCE = 0.9;
	static final double NEW_DOC_CHANCE = 0.92;
	static final double CHANGE_DOC_CHANCE = 0.96;
	static final double SHARE_DOC_CHANCE = 1.0;

	public static class UserActivity extends Thread {
		private UserManager um;
		private int userNum;
		private int docNum;
		private User u;
		public UserActivity(UserManager um,int userNum) {
			System.out.println("Creating user " + userNum);
			this.um = um;	this.userNum = userNum;	this.docNum = 0;
			u = um.createUser("user" + userNum,"pass" + userNum);
		}

		@Override
		public void run() {
			Doc d = null;
			//now we perform operations
			for(int i = 0; i < EVENTS_PER_USER; i++) {
				double action = rand.nextDouble();
				if(d == null) {
					action = NEW_DOC_CHANCE;
					System.out.println("Starting user " + userNum);
				}

				if(action <= EDIT_CHANCE) {
					System.out.println("user " + userNum + " editing " + d);
					long temp = System.nanoTime();
					d.insert(u,"I am user " + userNum + "\n");
					System.out.println(System.nanoTime() - temp);
				} else if (action <= NEW_DOC_CHANCE) {
					System.out.println("user " + userNum + " creating new doc");
					try {
					d = u.newDoc("(doc " + docNum + " from user " + userNum +")");
					} catch(Exception ex) {System.out.println("Creating a new doc caused exception - " + ex);}
					docNum++;
					System.out.println("user " + userNum + " created " + d);
				} else if (action <= CHANGE_DOC_CHANCE) {
					System.out.println("user " + userNum + " switching docs");
					for(Data newData : u.getOfferedData())
						u.acceptData(newData);
					int choice = rand.nextInt(u.getData().size());
					for(Data choiceData : u.getData()) {
						if(choice == 0)
							d = (Doc) choiceData;
						choice--;
					}
				} else { //(action <= SHARE_DOC_CHANCE) 
					int choice = rand.nextInt(USER_NUM);
					System.out.println("user " + userNum + " sharing " + d + " with user " + choice);
					try {
						um.shareData("user"+choice, u, d, Data.Permissions.SHARE);
					} catch(Exception ex) {} //should ignore cannot change people from owner to share
				}
			}
		}
	}
}




