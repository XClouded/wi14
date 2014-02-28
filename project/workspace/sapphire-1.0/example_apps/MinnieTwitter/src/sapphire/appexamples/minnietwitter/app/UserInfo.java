package sapphire.appexamples.minnietwitter.app;

import java.io.Serializable;
import java.security.MessageDigest;

public class UserInfo implements Serializable {
	String name;
	String screenName;
	String description;
	int followersCount = 0;
	int followingCount = 0;
	int tweetsCount = 0;

	/* MDH5 of password*/
	byte[] password;

	public UserInfo(String name, String screenName, String description, String passwd) {
		this.name = name;             // full name
		this.screenName = screenName; // username
		this.description = description;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			this.password = md.digest(passwd.getBytes("UTF-8"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getDescription() {
		return description;
	}

	public String getLocation() {
		return null;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public byte[] getPassword() {
		return password;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}

	public int getFollowersCount() {
		return followersCount;
	}

	public void setFollowersCount(int followersCount) {
		this.followersCount = followersCount;
	}

	public int getFollowingCount() {
		return followingCount;
	}

	public void setFollowingCount(int followingCount) {
		this.followingCount = followingCount;
	}

	public int getTweetsCount() {
		return tweetsCount;
	}

	public void setTweetsCount(int tweetsCount) {
		this.tweetsCount = tweetsCount;
	}
}
