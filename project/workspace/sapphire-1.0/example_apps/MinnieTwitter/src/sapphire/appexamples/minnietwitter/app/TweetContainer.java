package sapphire.appexamples.minnietwitter.app;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class TweetContainer implements Serializable {

	private Tweet tweet;
	private List<User> favorited = null;
	private List<User> retweeted = null;
	private UserManager um = null;
	private int favoritedCount;
	private int retweetedCount;

	public TweetContainer(Tweet t, TagManager tm, UserManager um) {
		favorited = new ArrayList<User>();
		favoritedCount = 0;
		retweeted = new ArrayList<User>();
		retweetedCount = 0;
		tweet = t;
		this.um = um;

		/* Parse the tweet and get the tags and the mentioned users */
		List<TweetEntity> tags = new ArrayList<TweetEntity>();
		List<TweetEntity> mentions = new ArrayList<TweetEntity>();
		String text = t.getText();

		//TODO: trim spaces
		int tokenStartIndex = 0;
		for (String word : text.split(" ")) {
			if (word.startsWith("#")) {
				tm.addTag(word, t);
				tags.add(new TweetEntity(TweetEntity.KEntityType.hashtags, tokenStartIndex, tokenStartIndex + word.length(), null));
			} else if (word.startsWith("@")) {
				// TODO: more efficient?
				User mentionedUser = um.getUser(word.substring(1));
				if (mentionedUser != null)
				{
					mentions.add(new TweetEntity(TweetEntity.KEntityType.user_mentions, tokenStartIndex, tokenStartIndex + word.length(), mentionedUser.getUserInfo().getName()));
					mentionedUser.mentionedBy(t);
				}
			}
			tokenStartIndex += word.length() + 1;
		}
		t.setTags(tags);
		t.setMentions(mentions);
	}

	public  TweetContainer(Tweet t) {
		tweet = t;
	}
	
	public void addFavorite(User u) {
		favorited.add(u);
		tweet.incFavorites();
		favoritedCount++;
	}
	
	public void removeFavorite(User u) {
		favorited.remove(u);
		tweet.decFavorites();
		favoritedCount--;
	}

	public void addRetweet(User u) {
		retweeted.add(u);
		tweet.incRetweetes();
		retweetedCount++;
	}

	public List<User> getFavorited(int from, int to) {
		return Util.checkedSubList(favorited, from, to);
	}

	public List<User> getRetweeted(int from, int to) {
		return Util.checkedSubList(retweeted, from, to);
	}

	public int getFavoritedCount() {
		return favoritedCount;
	}

	public int getRetweetedCount() {
		return retweetedCount;
	}

	public Tweet getTweet() {
		return tweet;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		try {
			final TweetContainer other = (TweetContainer) obj;
			tweet.equals(other.getTweet());
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
}
