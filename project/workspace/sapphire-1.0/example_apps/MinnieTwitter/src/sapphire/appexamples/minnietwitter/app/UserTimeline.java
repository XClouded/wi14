package sapphire.appexamples.minnietwitter.app;

import java.util.Date;

public class UserTimeline extends Timeline {
	private int currentId;

	public UserTimeline(User owner, String ownerUserName, TagManager tm, UserManager um) {
		super(owner, ownerUserName, tm, um);
		currentId = -1;
	}

	/* Publishes a tweet */
	public Tweet tweet(String text, String source) {
		currentId++;
		return super.addTweet(text, source, currentId);
	}

	/* Removes the tweet from the author's user timeline */
	public void delete(String authorScreenName, int pos) {
		super.removeTweet(new Tweet(null, null, authorScreenName, pos, null, null));
	}
	
	/* Retweets a tweet */
	public Tweet retweet(String authorScreenName, int pos) {
		User u = userManager.getUser(authorScreenName);
		// TODO: make chain call work
		// Tweet t = u.getUserTimeline().retweetedBy(owner, pos);
		Tweet t= u.retweetedBy(owner, pos);
		currentId++;
		t.setNewPos(currentId);
		t.setRetweeterName(ownerUserName);
		super.addTweet(t);
		return t;
	}

	/* One of this userTimeline owner tweets (with id pos) was retweeted by user u */
	public Tweet retweetedBy(User u, int pos) {
		TweetContainer tc = tweets.get(pos);
		tc.addRetweet(u);
		System.out.println("retweetedBy: @" + u.getUserInfo().getScreenName() + " retweeted: " + tc.getTweet().getText());
		return tc.getTweet();
	}

	/* One of this userTimeline owner tweets (with id pos) was favorited by user u */
	public Tweet favoritedBy(User u, int pos, boolean favorite) {
		TweetContainer tc = tweets.get(pos);
		if (favorite) {
			tc.addFavorite(u);
			System.out.println("favoritedBy: @" + u.getUserInfo().getScreenName() + " favorited: " + tc.getTweet().getText());
		}
		else {
			tc.removeFavorite(u);
			System.out.println("favoritedBy: @" + u.getUserInfo().getScreenName() + " unfavorited: " + tc.getTweet().getText());			
		}
		return tc.getTweet();
	}
}
