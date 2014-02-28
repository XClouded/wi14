package sapphire.appexamples.minnietwitter.device;

import java.math.BigInteger;
import java.util.List;

import sapphire.app.SapphireActivity;
import sapphire.app.SapphireObject;
import sapphire.appexamples.minnietwitter.app.Timeline;
import sapphire.appexamples.minnietwitter.app.Tweet;
import sapphire.appexamples.minnietwitter.app.TwitterManager;
import sapphire.appexamples.minnietwitter.app.User;
import sapphire.appexamples.minnietwitter.app.UserManager;

public class TwitterActivityOne implements SapphireActivity {
	
	public void onCreate(SapphireObject appEntryPoint) {
		try {
			
			TwitterManager tm = (TwitterManager) appEntryPoint;
            System.out.println("Received Twitter Manager Stub: " + tm);
            
            UserManager userManager = tm.getUserManager();
            System.out.println("Received User Manager Stub: " + userManager);
            
            User aaasz = userManager.addUser("aaasz",  "aaasz", "Adriana Szekeres", "Student enjoying life.");
            aaasz.initialize(aaasz, userManager); //workaround for this_ operator
            User iyzhang = userManager.addUser("iyzhang",  "iyzhang", "Irene Zhang", "Student enjoying life. Ex-VMWare :D");
            iyzhang.initialize(iyzhang, userManager);
            
            aaasz.tweet("Nice times. #lifeisgood", "Android");
            aaasz.tweet("At UW. #working", "Android");
            
            aaasz.addFollowing(iyzhang);
            
            iyzhang.tweet("Sapphiring.. with @aaasz #lotsofwork", "Android");
            iyzhang.tweet("Were's Arvind?.. . @aaasz #paperproblems", "Android");
        
            System.out.println("Done populating!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

//Feed feed = me.getFeed();		
// Obs. Better to get Feed if used often than call me.getFeed().tweet => two RMIs becomes more expensive