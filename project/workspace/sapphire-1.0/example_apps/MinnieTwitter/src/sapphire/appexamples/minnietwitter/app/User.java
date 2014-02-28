package sapphire.appexamples.minnietwitter.app;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import sapphire.app.SapphireObject;

public class User implements SapphireObject {
	private final int FOLLOWERS_PART = 4000;

	private HomeTimeline homeTimeline;
	private UserTimeline userTimeline;
	private FavoritesTimeline favoritesTimeline;
	private MentionsTimeline mentionsTimeline;

	private UserInfo ui;
	private User me;

	// TODO: specify the client policy per instance - cache only the screenname
	List<User> followers; // people who follow me
	List<User> following; // people me follows

	TagManager tagManager;
	UserManager userManager;

	public User(UserInfo ui, TagManager tm) {
		this.ui = ui;
		followers = new ArrayList<User>();
		following = new ArrayList<User>();
		tagManager = tm;
	}

	public void initialize(User u, UserManager userManager) {
		homeTimeline = new HomeTimeline(u, ui.getScreenName(), tagManager, userManager);
		userTimeline = new UserTimeline(u, ui.getScreenName(), tagManager, userManager);
		favoritesTimeline = new FavoritesTimeline(u, ui.getScreenName(), tagManager, userManager);
		mentionsTimeline = new MentionsTimeline(u, ui.getScreenName(), tagManager, userManager);
		me = u;
		this.userManager = userManager;
	}

	/* INTERNAL FUNCTIONS */

	// A tweet must appear in this user's homeTimeline
	public void homeTweet(Tweet t) {
		homeTimeline.tweet(t);
	}

	// A tweet of this user was retweeted by user u.
	public Tweet retweetedBy(User u, int pos) {
		return userTimeline.retweetedBy(u, pos);
	}

	// A tweet of this user was favorited by user u.
	public Tweet favoritedBy(User u, int pos, boolean favorited) {
		return userTimeline.favoritedBy(u, pos, favorited);
	}
	
	// This user was mentioned by Tweet t
	public void mentionedBy(Tweet t) {
		mentionsTimeline.mentionedBy(t);
	}

	/* PUBLIC API */

	public List<User> getFollowers(int from, int to) {
		return Util.checkedSubList(followers, from, to);
	}

	public List<User> getFollowing(int from, int to) {
		return Util.checkedSubList(following, from, to);
	}

	public List<User> getFollowers() {
		return followers;
	}

	public List<User> getFollowing() {
		return following;
	}

	public void addFollower(User u) {
		System.out.println("@" + me.getUserInfo().getScreenName() + " is followed by @" + u.getUserInfo().getScreenName());
		followers.add(u);
		ui.setFollowersCount(followers.size());
	}

	public void addFollowing(User u) {
		System.out.println("@" + me.getUserInfo().getScreenName() + " follows @" + u.getUserInfo().getScreenName());
		following.add(u);
		u.addFollower(me);
		ui.setFollowingCount(following.size());
	}

	public void removeFollower(User u) {
		// TODO: study how to express what to cache - caching whole User object is big
		followers.remove(u);
		ui.setFollowersCount(followers.size());
	}

	public void removeFollowing(User u) {
		// TODO: Transactions?
		// TODO: study how to express what to cache - caching whole User object is big
		following.remove(u);
		u.removeFollower(me);
		ui.setFollowingCount(following.size());
	}

	public UserInfo getUserInfo() {
		return ui;
	}

	public List<Tweet> getHomeTimeline(int from, int to) {
		return homeTimeline.getTweets(from, to);
	}

	public List<Tweet> getUserTimeline(int from, int to) {
		return userTimeline.getTweets(from, to);
	}

	public List<Tweet> getFavoritesTimeline(int from, int to) {
		return favoritesTimeline.getTweets(from, to);
	}

	public List<Tweet> getMentionsTimeline(int from, int to) {
		return mentionsTimeline.getTweets(from, to);
	}

	public Tweet tweet(String text, String source) {
		Tweet t = userTimeline.tweet(text, source);
		System.out.println("@" + me.getUserInfo().getScreenName() + " tweeted: " + text);
		deliverTweet(t);
		return t;
	}

	public void delete(String authorScreenName, int pos) {
		// Delete from our own timeline
		userTimeline.delete(authorScreenName, pos);
		ui.setTweetsCount(userTimeline.getTweetsCount());
		System.out.println("OK - Tweet deleted");
		// The copies of the tweet text in the other timelines
		// are kept
	}

	public Tweet retweet(String screenName, int pos) {
		Tweet t = userTimeline.retweet(screenName, pos);
		homeTimeline.retweet(screenName, pos);
		deliverTweet(t);
		return t;
	}

	// A tweet has been favorited/unfavorited by this user
	public Tweet favorite(String screenName, int pos, boolean favorite) {
		Tweet t = favoritesTimeline.favorite(screenName, pos, favorite);
		homeTimeline.favorite(screenName, pos, favorite);
		return t;
	}

	/* PRIVATE FUNTIONS */

	private void deliverTweet(Tweet t) {
		/* Start populating the home timelines of followers */
		//TODO: what happens when deleting.. and we use from, to?
		//TODO: lock followers
		//TODO: put in processing queue
		int size = followers.size();
		int i = 0;

		while (i * FOLLOWERS_PART < size) {
			List<User> f = Util.checkedSubList(followers, i * FOLLOWERS_PART, (i + 1) * FOLLOWERS_PART);
			new DeliveryThread(f, t).start();
			i++;
		}

		ui.setTweetsCount(userTimeline.getTweetsCount());
	}

	class DeliveryThread extends Thread {
		List<User> followers;
		Tweet t;

		public DeliveryThread (List<User> followers, Tweet t) {
			this.followers = followers;
			this.t = t;
		}

		@Override
		public void run() {
			for (User u : followers) {
				// TODO: Make the chain call work
				//u.getHomeTimeline().tweet(t);
				u.homeTweet(t);
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		try {
			final User other = (User) obj;
			if ((this.getUserInfo().getScreenName() == null) ? 
					(other.getUserInfo().getScreenName() != null) : !(this.getUserInfo().getScreenName().equals(other.getUserInfo().getScreenName())))
				return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
