package sapphire.appexamples.minnietwitter.app;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class Tweet implements Serializable {
	// {author, pos} - unique tweet ID
	private User author;
	private int pos;               // position in the userTimeline of the author
	private String authorScreenName;

	private String retweeterName = null;   //if the tweet was retweeted
	private int newPos;                    // position in the user timeline of the retweeter

	private int homePos; // position in the home timeline of the user
	private int favoritePos; //position in the favorites timeline of the user
	private int mentionsPos; //position in the mentions timeline of the user

	private int favorites = 0;
	private int retweetes = 0;
	private String text;
	private List<TweetEntity> tags;
	private List<TweetEntity> mentions;
	private Date createdAt;
	private String source;

	private Tweet original = null; // a pointer to the original tweet (instead of a copy) - not possible now....

	private boolean retweeted = false; //if this tweet was retweeted by the owner of the home timeline
	private boolean favorited = false; // if this tweet was favorited by the owner of the timeline

	public Tweet(String text, User author, String authorScreenName, int pos, Date createdAt, String source) {
		this.text = text;
		this.author = author;
		this.authorScreenName = authorScreenName;
		this.pos = pos;
		this.createdAt = createdAt;
		this.source = source;
	}

	public List<TweetEntity> getTags() {
		return tags;
	}

	public void setTags(List<TweetEntity> tags) {
		this.tags = tags;
	}

	public List<TweetEntity> getMentions() {
		return mentions;
	}

	public void setMentions(List<TweetEntity> mentions) {
		this.mentions = mentions;
	}

	public List<TweetEntity> getTweetEntities(TweetEntity.KEntityType type) {
		switch (type) {
		case hashtags:
			return tags;
		case user_mentions:
			return mentions;
		default:
			return null;
		}
	}

	public String getText() {
		return text;
	}

	public String getSource() {
		return source;
	}

	public User getAuthor() {
		return author;
	}

	public String getAuthorScreenName() {
		return authorScreenName;
	}

	public String getRetweeterName() {
		return retweeterName;
	}

	public void setRetweeterName(String retweeterName) {
		this.retweeterName = retweeterName;
	}

	public int getNewPos() {
		return newPos;
	}

	public void setNewPos(int newPos) {
		this.newPos = newPos;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public int getHomePos() {
		return homePos;
	}

	public void setHomePos(int homePos) {
		this.homePos = homePos;
	}

	public int getFavoritePos() {
		return favoritePos;
	}

	public void setFavoritePos(int favoritePos) {
		this.favoritePos = favoritePos;
	}

	public int getMentionsPos() {
		return mentionsPos;
	}

	public void setMentionsPos(int mentionsPos) {
		this.mentionsPos = mentionsPos;
	}

	public boolean isRetweet() {
		return retweeterName != null;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public Tweet getOriginal() {
		//return author.getTweet(pos);
		return original;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public int getFavorites() {
		return favorites;
	}

	public void incFavorites() {
		favorites++;
	}
	
	public void decFavorites() {
		favorites--;
	}
	
	public int getRetweetes() {
		return retweetes;
	}

	public void incRetweetes() {
		retweetes += 1;
	}

	public boolean isRetweeted() {
		return retweeted;
	}

	public void setRetweeted(boolean retweeted) {
		this.retweeted = retweeted;
	}

	public boolean isFavorite() {
		return favorited;
	}

	public void setFavorite(boolean favorited) {
		this.favorited = favorited;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		try {
			final Tweet other = (Tweet) obj;
			if (!this.authorScreenName.equals(other.getAuthorScreenName()) || !(this.pos == other.pos))
				return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}