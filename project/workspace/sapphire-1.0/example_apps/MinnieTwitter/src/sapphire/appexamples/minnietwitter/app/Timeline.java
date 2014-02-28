package sapphire.appexamples.minnietwitter.app;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import sapphire.app.SapphireObject;

public class Timeline implements Serializable {
	private final int DEFAULT_TWEETS_COUNT = 20;

	protected User owner;	            // owner of this timeline
	protected String ownerUserName;     // username of the owner of this timeline
	protected UserManager userManager;  // user manager

	/* TODO: Distributed structure for the tweets? */
	/* TODO: Locking */
	protected List<TweetContainer> tweets = null;
	protected int tweetsCount;

	protected TagManager tagManager;

	public Timeline(User owner, String ownerUserName, TagManager tm, UserManager um) {
		this.owner = owner;
		this.ownerUserName = ownerUserName;
		this.tagManager = tm;
		this.userManager = um;
		this.tweets = new ArrayList<TweetContainer>();
		this.tweetsCount = 0;
	}

	public Tweet addTweet(String text, String source, int tweetId) {
		Tweet t = new Tweet(text, owner, ownerUserName, tweetId, new Date(), source);
		TweetContainer tc = new TweetContainer(t, tagManager, userManager);
		tweets.add(tc);
		tweetsCount++;
		return t;
	}

	public void addTweet(Tweet t) {
		TweetContainer tc = new TweetContainer(t);
		tweets.add(tc);
		tweetsCount++;
	}

	public void removeTweet(Tweet t) {
		tweets.remove(new TweetContainer(t));
		tweetsCount--;
	}

	public List<Tweet> getTweets(int from, int to) {
		if (to == -1)
			to = DEFAULT_TWEETS_COUNT;
		if (from == -1) {
			from = tweets.size() - to;
			to = tweets.size() - 1;
		}

		List<TweetContainer> tc = Util.checkedSubList(tweets, from, to);
		List<Tweet> tw = new ArrayList<Tweet>();
		for (TweetContainer c : tc)
			tw.add(c.getTweet());
		return tw;
	}

	public int getTweetsCount() {
		return tweetsCount;
	}

	public User getOwner() {
		return owner;
	}
}
