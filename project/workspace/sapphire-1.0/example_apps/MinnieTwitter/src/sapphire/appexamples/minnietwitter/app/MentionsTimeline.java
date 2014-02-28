package sapphire.appexamples.minnietwitter.app;

public class MentionsTimeline extends Timeline {
	private int currentId;

	public MentionsTimeline(User owner, String ownerUserName, TagManager tm, UserManager um) {
		super(owner, ownerUserName, tm, um);
		currentId = -1;
	}

	public void mentionedBy(Tweet t) {
		t.setMentionsPos(currentId);
		super.addTweet(t);
	}
}
