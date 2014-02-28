package sapphire.appexamples.minnietwitter.app;

public class FavoritesTimeline extends Timeline {
	private int currentId;

	public FavoritesTimeline(User owner, String ownerUserName, TagManager tm, UserManager um) {
		super(owner, ownerUserName, tm, um);
		currentId = -1;
	}

	/* Favorites a tweet */
	public Tweet favorite(String authorScreenName, int pos, boolean favorite) {
		User u = userManager.getUser(authorScreenName);
		// TODO: make chain call work?
		// Tweet t = u.getUserTimeline().retweetedBy(owner, pos);
		Tweet t = u.favoritedBy(owner, pos, favorite);
		if (favorite) {
			currentId++;
			t.setFavoritePos(currentId);
			t.setFavorite(favorite);
			super.addTweet(t);
		} else {
			super.removeTweet(t);
		}
		return t;
	}
}
