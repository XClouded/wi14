package sapphire.appexamples.minnietwitter.device.twimight.global;

import sapphire.appexamples.minnietwitter.app.TagManager;
import sapphire.appexamples.minnietwitter.app.TwitterManager;
import sapphire.appexamples.minnietwitter.app.User;
import sapphire.appexamples.minnietwitter.app.UserManager;

public final class StateSingleton {
	    private static final StateSingleton instance = new StateSingleton();
	    public static StateSingleton getInstance() { return instance; }

		private UserManager um = null;
	    private TagManager tagManager = null;
	    private User user = null;
	    private TwitterManager tm = null;

	    public TwitterManager getTwitterManager() {
			return tm;
		}

		public void setTwitterManager(TwitterManager tm) {
			this.tm = tm;
		}

		public UserManager getUserManager() {
			return um;
		}

		public void setUserManager(UserManager um) {
			this.um = um;
		}

		public TagManager getTagManager() {
			return tagManager;
		}

		public void setTagManager(TagManager tagManager) {
			this.tagManager = tagManager;
		}

	    public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}

}
