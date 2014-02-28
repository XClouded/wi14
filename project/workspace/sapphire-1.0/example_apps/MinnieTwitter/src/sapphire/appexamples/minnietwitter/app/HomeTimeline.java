package sapphire.appexamples.minnietwitter.app;

public class HomeTimeline extends Timeline {
	private int currentId;

	public HomeTimeline(User owner, String ownerUserName, TagManager tm, UserManager um) {
		super(owner, ownerUserName, tm, um);
		currentId = -1;
	}

	// One of the people I follow (friend) posted something so it should be added to my homeTimeline
	public void tweet(Tweet t) {
		TweetContainer tc = new TweetContainer(t, tagManager, userManager);
		currentId++;
		t.setHomePos(currentId);
		tweets.add(tc);
		System.out.println("New tweet in feed: " + t.getText());
	}

	public void retweet(String authorScreenName, int pos) {
		// A tweet has been retweeted by the owner of this homeTimeline, so we check if the tweet was in the homeTimeline to
		// mark it as a retweet
		for (TweetContainer tc : tweets) {
			Tweet tt = tc.getTweet();
			if (tt.getAuthorScreenName().equals(authorScreenName) && (tt.getPos() == pos)) {
				System.out.println("OK - set retweeted flag");
				tt.setRetweeted(true);
				tt.setRetweeterName(ownerUserName);
				break;
			}
		}
	}

	public void favorite(String authorScreenName, int pos, boolean favorite) {
		// A tweet has been favorited by the owner of this homeTimeline, so we check if the tweet was in the homeTimeline to
		// mark it as favorited
		for (TweetContainer tc : tweets) {
			Tweet tt = tc.getTweet();
			if (tt.getAuthorScreenName().equals(authorScreenName) && (tt.getPos() == pos)) {
				System.out.println("OK - set favorite flag to: "  + favorite);
				tt.setFavorite(favorite);
				break;
			}
		}
	}
}
