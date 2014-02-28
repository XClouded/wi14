package sapphire.appexamples.minnietwitter.app;

import java.io.Serializable;

public class TweetEntity implements Serializable {

	public static enum KEntityType {
		hashtags, urls, user_mentions
	}

	final String display;

	/**
	 * end of the entity in the contents String, exclusive
	 */
	public final int end;
	/**
	 * start of the entity in the contents String, inclusive
	 */
	public final int start;

	public final KEntityType type;

	TweetEntity(KEntityType type, int start, int end, String display) {
		this.end = end;
		this.start = start;
		this.type = type;
		this.display = display;
	}

	public String displayVersion() {
		return display;
	}
}
