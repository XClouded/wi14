/*******************************************************************************
 * Copyright (c) 2011 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Paolo Carta - Implementation
 *     Theus Hossmann - Implementation
 *     Dominik Schatzmann - Message specification
 ******************************************************************************/

package sapphire.appexamples.minnietwitter.device.twimight.net.twitter;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import sapphire.appexamples.minnietwitter.device.twimight.R;
import sapphire.appexamples.minnietwitter.device.twimight.activities.LoginActivity;
import sapphire.appexamples.minnietwitter.device.twimight.activities.ShowTweetListActivity;
import sapphire.appexamples.minnietwitter.device.twimight.activities.TwimightBaseActivity;
import sapphire.appexamples.minnietwitter.device.twimight.data.DBOpenHelper;
import sapphire.appexamples.minnietwitter.device.twimight.fragments.TweetListFragment;
import sapphire.appexamples.minnietwitter.device.twimight.net.opportunistic.ScanningAlarm;
import sapphire.appexamples.minnietwitter.device.twimight.net.opportunistic.ScanningService;
import sapphire.appexamples.minnietwitter.device.twimight.net.tds.TDSService;
import sapphire.appexamples.minnietwitter.device.twimight.security.CertificateManager;
import sapphire.appexamples.minnietwitter.device.twimight.security.KeyManager;
import sapphire.appexamples.minnietwitter.device.twimight.util.Constants;

/**
 * The content provider for all kinds of tweets (normal, disaster, favorites, mentions). 
 * The URIs and column names are defined in class Tweets.
 * @author thossmann
 * @author pcarta
 */
public class TweetsContentProvider extends ContentProvider {

	private static final String TAG = "TweetsContentProvider";

	private SQLiteDatabase database;
	private DBOpenHelper dbHelper;
	private String localScreenName;

	private static final String LAST_CACHE_ALL_PAGES = "lastCacheAllPages";

	private static UriMatcher tweetUriMatcher;

	private static final int TWEETS = 1;
	private static final int TWEETS_ID = 2;


	private static final int TWEETS_TIMELINE_NORMAL = 4;
	private static final int TWEETS_TIMELINE_DISASTER = 5;
	private static final int TWEETS_TIMELINE_ALL = 6;
	private static final int TWEETS_TIMELINE_NEW = 7;
	private static final int TWEETS_TIMELINE_DISASTER_NEW =11 ;

	private static final int TWEETS_FAVORITES_NORMAL = 8;
	private static final int TWEETS_FAVORITES_DISASTER = 9;
	private static final int TWEETS_FAVORITES_ALL = 10;


	private static final int TWEETS_MENTIONS_NORMAL = 12;
	private static final int TWEETS_MENTIONS_DISASTER = 13;
	private static final int TWEETS_MENTIONS_ALL = 14;

	private static final int TWEETS_USER_ID = 15;

	private static final int TWEETS_SEARCH = 16;

	// Here we define all the URIs this provider knows
	static{		

		tweetUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS, TWEETS);

		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/#", TWEETS_ID);

		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/" + Tweets.SEARCH, TWEETS_SEARCH);	

		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/" + Tweets.TWEETS_TABLE_USER + "/*", TWEETS_USER_ID);


		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/" + Tweets.TWEETS_TABLE_TIMELINE 
				+ "/" + Tweets.TWEETS_SOURCE_NORMAL, TWEETS_TIMELINE_NORMAL);

		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/" + Tweets.TWEETS_TABLE_TIMELINE 
				+ "/" + Tweets.TWEETS_SOURCE_DISASTER, TWEETS_TIMELINE_DISASTER);

		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/" + Tweets.TWEETS_TABLE_TIMELINE 
				+ "/" + Tweets.TWEETS_SOURCE_ALL, TWEETS_TIMELINE_ALL);

		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/" + Tweets.TWEETS_TABLE_TIMELINE 
				+ "/" + Tweets.TWEETS_SINCE_LAST_UPDATE, TWEETS_TIMELINE_NEW);

		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/" + Tweets.TWEETS_TABLE_TIMELINE 
				+ "/" + Tweets.TWEETS_SOURCE_DISASTER + "/" + Tweets.TWEETS_SINCE_LAST_UPDATE, TWEETS_TIMELINE_DISASTER_NEW );

		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/" + Tweets.TWEETS_TABLE_FAVORITES 
				+ "/" + Tweets.TWEETS_SOURCE_NORMAL, TWEETS_FAVORITES_NORMAL);

		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/" + Tweets.TWEETS_TABLE_FAVORITES 
				+ "/" + Tweets.TWEETS_SOURCE_DISASTER, TWEETS_FAVORITES_DISASTER);

		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/" + Tweets.TWEETS_TABLE_FAVORITES 
				+ "/" + Tweets.TWEETS_SOURCE_ALL, TWEETS_FAVORITES_ALL);



		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/" + Tweets.TWEETS_TABLE_MENTIONS 
				+ "/" + Tweets.TWEETS_SOURCE_NORMAL, TWEETS_MENTIONS_NORMAL);

		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/" + Tweets.TWEETS_TABLE_MENTIONS 
				+ "/" + Tweets.TWEETS_SOURCE_DISASTER, TWEETS_MENTIONS_DISASTER);

		tweetUriMatcher.addURI(Tweets.TWEET_AUTHORITY, Tweets.TWEETS + "/" + Tweets.TWEETS_TABLE_MENTIONS 
				+ "/" + Tweets.TWEETS_SOURCE_ALL, TWEETS_MENTIONS_ALL);


	}

	// for the status bar notification
	private static final int TWEET_NOTIFICATION_ID = 1;	
	private static final int NOTIFY_MENTION = 2;
	private static final int NOTIFY_DISASTER = 3;
	private static final int NOTIFY_TWEET = 4;

	/**
	 * onCreate we initialize and open the DB.
	 */
	@Override
	public boolean onCreate() {
		dbHelper = DBOpenHelper.getInstance(getContext().getApplicationContext());
		database = dbHelper.getWritableDatabase();
		localScreenName = LoginActivity.getTwitterScreenname(getContext());
		return true;
	}



	/**
	 * Returns the MIME types (defined in Tweets) of a URI
	 */
	@Override
	public String getType(Uri uri) {
		switch(tweetUriMatcher.match(uri)){
		case TWEETS: return Tweets.TWEETS_CONTENT_TYPE;

		case TWEETS_ID: return Tweets.TWEET_CONTENT_TYPE;			

		case TWEETS_USER_ID: return Tweets.TWEETS_CONTENT_TYPE;

		case TWEETS_TIMELINE_NORMAL: return Tweets.TWEETS_CONTENT_TYPE;
		case TWEETS_TIMELINE_DISASTER: return Tweets.TWEETS_CONTENT_TYPE;
		case TWEETS_TIMELINE_ALL: return Tweets.TWEETS_CONTENT_TYPE;

		case TWEETS_FAVORITES_NORMAL: return Tweets.TWEETS_CONTENT_TYPE;
		case TWEETS_FAVORITES_DISASTER: return Tweets.TWEETS_CONTENT_TYPE;
		case TWEETS_FAVORITES_ALL: return Tweets.TWEETS_CONTENT_TYPE;

		case TWEETS_MENTIONS_NORMAL: return Tweets.TWEETS_CONTENT_TYPE;
		case TWEETS_MENTIONS_DISASTER: return Tweets.TWEETS_CONTENT_TYPE;
		case TWEETS_MENTIONS_ALL: return Tweets.TWEETS_CONTENT_TYPE;

		default: throw new IllegalArgumentException("Unknown URI: " + uri);	
		}
	}

	/**
	 * Reads the timestamp of the last timeline update from shared preferences.
	 * @return
	 */
	public static long getLastCacheAllPages(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getLong(LAST_CACHE_ALL_PAGES, 0);
	}

	/**
	 * Stores the current timestamp as the time of last timeline update
	 */
	public static void setLastCacheAllPages(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor prefEditor = prefs.edit();		
		prefEditor.putLong(LAST_CACHE_ALL_PAGES, System.currentTimeMillis());

		prefEditor.commit();
	}



	/**
	 * Query the timeline table
	 * TODO: Create the queries more elegantly..
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs, String sortOrder) {

		if(TextUtils.isEmpty(sortOrder)) sortOrder = Tweets.DEFAULT_SORT_ORDER;

		Cursor c;
		String sql;
		Intent i;
		switch(tweetUriMatcher.match(uri)){

		case TWEETS_TIMELINE_DISASTER_NEW:				    	
			long timestamp_dis = TDSService.getLastUpdate(getContext());
			sql = Tweets.COL_BUFFER+" & ("+Tweets.BUFFER_DISASTER + "|" + Tweets.BUFFER_MYDISASTER + ") != 0 AND " + 
					Tweets.COL_RECEIVED + " >= " + timestamp_dis; 
			Log.i(TAG,"sql: " + sql);
			c = database.query(DBOpenHelper.TABLE_TWEETS, null, sql, null, null, null, Tweets.DEFAULT_SORT_ORDER);
			break;

		case TWEETS_TIMELINE_NEW:		    	
			long timestamp = getLastCacheAllPages(getContext());
			sql = Tweets.COL_RECEIVED + "> " + timestamp +" and " + Tweets.COL_HTML_PAGES + " = " + 1 ;
			c = database.query(DBOpenHelper.TABLE_TWEETS, null, sql, null, null, null, null);
			setLastCacheAllPages(getContext());

			break;

		case TWEETS: 
			if (TwimightBaseActivity.D) Log.d(TAG, "Query TWEETS");
			c = database.query(DBOpenHelper.TABLE_TWEETS, projection, where, whereArgs, null, null, sortOrder);
			c.setNotificationUri(getContext().getContentResolver(), Tweets.ALL_TWEETS_URI);
			break;

		case TWEETS_ID:
			if (TwimightBaseActivity.D) Log.d(TAG, "Query TWEETS_ID " + uri.getLastPathSegment());
			sql = "SELECT "
					+ DBOpenHelper.TABLE_TWEETS + "." + "_id AS _id, "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_USER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MENTIONS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT_PLAIN + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_CREATED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_SOURCE + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_REPLYTO + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FAVORITED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETCOUNT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_LAT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_LNG + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FLAGS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_BUFFER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MEDIA + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_HTML_PAGES + ", "				
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_ISVERIFIED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED_BY + ", "
					+ DBOpenHelper.TABLE_USERS + "." + "_id AS userRowId, "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_ID + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_SCREENNAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_NAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_PROFILEIMAGE_PATH + " "
					+ "FROM "+DBOpenHelper.TABLE_TWEETS + " "
					+ "JOIN " + DBOpenHelper.TABLE_USERS + " " 
					+ "ON " +DBOpenHelper.TABLE_TWEETS+"." +Tweets.COL_SCREENNAME+ "=" +DBOpenHelper.TABLE_USERS+"." +TwitterUsers.COL_SCREENNAME + " "
					+ "WHERE " + DBOpenHelper.TABLE_TWEETS+ "._id=" + uri.getLastPathSegment() + ";";
			c = database.rawQuery(sql, null);
			c.setNotificationUri(getContext().getContentResolver(), uri);

			break;

		case TWEETS_SEARCH: // the search query must be given in the where argument
			if (TwimightBaseActivity.D) Log.d(TAG, "Query SEARCH");
			sql = "SELECT "
					+ DBOpenHelper.TABLE_TWEETS + "." + "_id AS _id, "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_USER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MENTIONS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT_PLAIN + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_CREATED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_SOURCE + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_REPLYTO + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FAVORITED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETCOUNT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_LAT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_LNG + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FLAGS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_BUFFER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MEDIA + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_HTML_PAGES + ", "		
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_DISASTERID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_ISVERIFIED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED_BY + ", "
					+ DBOpenHelper.TABLE_USERS + "." + "_id AS userRowId, "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_ID + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_SCREENNAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_NAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_PROFILEIMAGE_PATH + " "
					+ "FROM "+DBOpenHelper.TABLE_TWEETS + " "
					+ "JOIN " + DBOpenHelper.TABLE_USERS + " " 
					+ "ON " + DBOpenHelper.TABLE_TWEETS+"." +Tweets.COL_SCREENNAME+ "=" +DBOpenHelper.TABLE_USERS+"." +TwitterUsers.COL_SCREENNAME + " "
					+ "WHERE  " + DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_TEXT+" LIKE '%" + where + "%' "
					+ "ORDER BY " + Tweets.DEFAULT_SORT_ORDER +";";
			c = database.rawQuery(sql, null);
			c.setNotificationUri(getContext().getContentResolver(), Tweets.TABLE_SEARCH_URI);

			//start synch service with a synch timeline request
			i = new Intent(TwitterService.SYNCH_ACTION);
			i.putExtra("synch_request", TwitterService.SYNCH_SEARCH_TWEETS);
			i.putExtra("query", where);
			getContext().startService(i);
			break;

		case TWEETS_TIMELINE_NORMAL:
			if (TwimightBaseActivity.D) Log.d(TAG, "Query TIMELINE_NORMAL");

			sql = "SELECT "
					+ DBOpenHelper.TABLE_TWEETS + "." + "_id AS _id, "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_USER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MENTIONS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT_PLAIN + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_CREATED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RECEIVED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_REPLYTO + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FAVORITED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MEDIA + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_HTML_PAGES + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FLAGS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_BUFFER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_DISASTERID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_ISVERIFIED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED_BY + ", "
					+ DBOpenHelper.TABLE_USERS + "." + "_id AS userRowId, "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_SCREENNAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_NAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_PROFILEIMAGE_PATH + " "
					+ "FROM "+DBOpenHelper.TABLE_TWEETS + " "
					+ "JOIN " + DBOpenHelper.TABLE_USERS + " " 
					+ "ON " +DBOpenHelper.TABLE_TWEETS+"." +Tweets.COL_SCREENNAME+ "=" +DBOpenHelper.TABLE_USERS+"." +TwitterUsers.COL_SCREENNAME+ " "
					+ "WHERE ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER+"&"+Tweets.BUFFER_TIMELINE+")!=0 "					
					+ "ORDER BY " + Tweets.REVERSE_SORT_ORDER
					+ " LIMIT 5;";
			c = database.rawQuery(sql, null);
			// TODO: Correct notification URI
			c.setNotificationUri(getContext().getContentResolver(), Tweets.TABLE_TIMELINE_URI);

			// start synch service with a synch timeline request
			i = new Intent(TwitterService.SYNCH_ACTION);
			i.putExtra("synch_request", TwitterService.SYNCH_TIMELINE);
			getContext().startService(i);
			break;
		case TWEETS_TIMELINE_DISASTER: 
			if (TwimightBaseActivity.D) Log.d(TAG, "Query TIMELINE_DISASTER");
			sql = "SELECT "
					+ DBOpenHelper.TABLE_TWEETS + "." + "_id AS _id, "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_USER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MENTIONS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT_PLAIN + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_CREATED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RECEIVED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_REPLYTO + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FAVORITED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MEDIA + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_HTML_PAGES + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FLAGS + ", "	
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_BUFFER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_DISASTERID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_ISVERIFIED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED_BY + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_CERTIFICATE + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_SIGNATURE + ", "
					+ DBOpenHelper.TABLE_USERS + "." + "_id AS userRowId, "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_SCREENNAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_NAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_PROFILEIMAGE_PATH + " "
					+ "FROM "+DBOpenHelper.TABLE_TWEETS + " "
					+ "JOIN " + DBOpenHelper.TABLE_USERS + " " 
					+ "ON " +DBOpenHelper.TABLE_TWEETS+"." +Tweets.COL_SCREENNAME+ "=" +DBOpenHelper.TABLE_USERS+"." +TwitterUsers.COL_SCREENNAME+ " "
					+ "WHERE ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER+"&"+Tweets.BUFFER_DISASTER+")!=0 "
					+ "OR ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER+"&"+Tweets.BUFFER_MYDISASTER+")!=0 "
					+ "ORDER BY " + Tweets.DEFAULT_SORT_ORDER +";";
			c = database.rawQuery(sql, null);
			// TODO: Correct notification URI
			c.setNotificationUri(getContext().getContentResolver(), Tweets.TABLE_TIMELINE_URI);

			// start synch service with a synch timeline request
			//i = new Intent(TwitterService.SYNCH_ACTION);
			//i.putExtra("synch_request", TwitterService.SYNCH_TIMELINE);
			//getContext().startService(i);
			break;
		case TWEETS_TIMELINE_ALL:
			if (TwimightBaseActivity.D) Log.d(TAG, "Query TIMELINE_ALL");
			sql = "SELECT "
					+ DBOpenHelper.TABLE_TWEETS + "." + "_id AS _id, "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_USER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MENTIONS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT_PLAIN + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_CREATED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RECEIVED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_REPLYTO + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FAVORITED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FLAGS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_BUFFER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MEDIA + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_HTML_PAGES + ", "				
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_DISASTERID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_ISVERIFIED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED_BY + ", "
					+ DBOpenHelper.TABLE_USERS + "." + "_id AS userRowId, "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_SCREENNAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_NAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_PROFILEIMAGE_PATH + " "
					+ "FROM "+DBOpenHelper.TABLE_TWEETS + " "
					+ "JOIN " + DBOpenHelper.TABLE_USERS + " " 
					+ "ON " +DBOpenHelper.TABLE_TWEETS+"." +Tweets.COL_SCREENNAME+ "=" +DBOpenHelper.TABLE_USERS+"." +TwitterUsers.COL_SCREENNAME+ " "
					+ "WHERE ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER+"&"+Tweets.BUFFER_DISASTER+")!=0 "
					+ "OR ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER+"&"+Tweets.BUFFER_MYDISASTER+")!=0 "
					+ "OR ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER+"&"+Tweets.BUFFER_TIMELINE+")!=0 "
					+ "ORDER BY " + Tweets.DEFAULT_SORT_ORDER +";";

			c = database.rawQuery(sql, null);				
			c.setNotificationUri(getContext().getContentResolver(), Tweets.TABLE_TIMELINE_URI);

			// start synch service with a synch timeline request
			i = new Intent(TwitterService.SYNCH_ACTION);
			i.putExtra("synch_request", TwitterService.SYNCH_TIMELINE);
			getContext().startService(i);
			break;

		case TWEETS_USER_ID:
			if (TwimightBaseActivity.D) Log.i(TAG, "Query TWEETS_USER_ID");

			// get the screenname for updating user tweets
			Uri userUri = Uri.parse("content://" + TwitterUsers.TWITTERUSERS_AUTHORITY + "/" + TwitterUsers.TWITTERUSERS);
			String[] userProjection = {TwitterUsers.COL_SCREENNAME};
			Cursor userCursor = getContext().getContentResolver().query(userUri, userProjection, TwitterUsers.COL_SCREENNAME+"=\""+uri.getLastPathSegment()+"\"", null, null);
			String screenName = "";				
			if(userCursor.getCount()>0) {
				userCursor.moveToFirst();
				screenName = userCursor.getString(userCursor.getColumnIndex(TwitterUsers.COL_SCREENNAME));
			}

			sql = "SELECT "
					+ DBOpenHelper.TABLE_TWEETS + "." + "_id AS _id, "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_USER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MENTIONS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT_PLAIN + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_CREATED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_REPLYTO + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FAVORITED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MEDIA + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_HTML_PAGES + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FLAGS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_BUFFER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_DISASTERID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_ISVERIFIED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED_BY + ", "
					+ DBOpenHelper.TABLE_USERS + "." + "_id AS userRowId, "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_SCREENNAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_NAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_PROFILEIMAGE_PATH + " "
					+ "FROM "+DBOpenHelper.TABLE_TWEETS + " "
					+ "JOIN " + DBOpenHelper.TABLE_USERS + " " 
					+ "ON " +DBOpenHelper.TABLE_TWEETS+"." +Tweets.COL_SCREENNAME+ "=" +DBOpenHelper.TABLE_USERS+"." +TwitterUsers.COL_SCREENNAME+ " "
					+ "WHERE ("+DBOpenHelper.TABLE_USERS+"."+TwitterUsers.COL_SCREENNAME+"=\""+uri.getLastPathSegment()+"\") " 
					+ "OR (" + DBOpenHelper.TABLE_TWEETS + "." + Tweets.COL_RETWEETED_BY + "='" + screenName + "') "
					+ "ORDER BY " + Tweets.DEFAULT_SORT_ORDER +";";

			//TODO: could be done be a separate thread
			c = database.rawQuery(sql, null);
			c.setNotificationUri(getContext().getContentResolver(), Tweets.TABLE_USER_URI);		


			if(userCursor.getCount()>0){					
				// start synch service with a synch user tweets request
				i = new Intent(TwitterService.SYNCH_ACTION);
				i.putExtra("synch_request", TwitterService.SYNCH_USERTWEETS);
				i.putExtra("screenname", screenName );
				getContext().startService(i); 
			}
			//userCursor.close();

			break;

		case TWEETS_FAVORITES_NORMAL: 
			if (TwimightBaseActivity.D) Log.d(TAG, "Query FAVORITES_NORMAL");
			sql = "SELECT "
					+ DBOpenHelper.TABLE_TWEETS + "." + "_id AS _id, "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_USER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MENTIONS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT_PLAIN + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_CREATED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_REPLYTO + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FAVORITED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FLAGS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_BUFFER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MEDIA + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_HTML_PAGES + ", "				
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_DISASTERID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_ISVERIFIED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED_BY + ", "
					+ DBOpenHelper.TABLE_USERS + "." + "_id AS userRowId, "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_SCREENNAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_NAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_PROFILEIMAGE_PATH + " "
					+ "FROM "+DBOpenHelper.TABLE_TWEETS + " "
					+ "JOIN " + DBOpenHelper.TABLE_USERS + " " 
					+ "ON " +DBOpenHelper.TABLE_TWEETS+"." +Tweets.COL_SCREENNAME+ "=" +DBOpenHelper.TABLE_USERS+"." +TwitterUsers.COL_SCREENNAME+ " "
					+ "WHERE ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER+"&" + Tweets.BUFFER_FAVORITES+")!=0 "
					+ "AND ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER + " & (" + Tweets.BUFFER_DISASTER+ "|"+ Tweets.BUFFER_MYDISASTER +") )=0 "
					+ "ORDER BY " + Tweets.DEFAULT_SORT_ORDER +";";
			c = database.rawQuery(sql, null);

			// TODO: correct notification URI
			c.setNotificationUri(getContext().getContentResolver(), Tweets.TABLE_FAVORITES_URI);

			// start synch service with a synch favorites request
			i = new Intent(TwitterService.SYNCH_ACTION);
			i.putExtra("synch_request", TwitterService.SYNCH_FAVORITES);
			getContext().startService(i);

			break;
		case TWEETS_FAVORITES_DISASTER: 
			if (TwimightBaseActivity.D) Log.d(TAG, "Query FAVORITES_DISASTER");
			sql = "SELECT "
					+ DBOpenHelper.TABLE_TWEETS + "." + "_id AS _id, "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_USER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MENTIONS + ", "					
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT_PLAIN + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_CREATED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_REPLYTO + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FAVORITED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FLAGS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_BUFFER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MEDIA + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_HTML_PAGES + ", "			
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_DISASTERID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_ISVERIFIED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED_BY + ", "
					+ DBOpenHelper.TABLE_USERS + "." + "_id AS userRowId, "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_SCREENNAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_NAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_PROFILEIMAGE_PATH + " "
					+ "FROM "+DBOpenHelper.TABLE_TWEETS + " "
					+ "JOIN " + DBOpenHelper.TABLE_USERS + " " 
					+ "ON " +DBOpenHelper.TABLE_TWEETS+"." +Tweets.COL_SCREENNAME+ "=" +DBOpenHelper.TABLE_USERS+"." +TwitterUsers.COL_SCREENNAME+ " "
					+ "WHERE ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER+"&"+Tweets.BUFFER_FAVORITES+")!=0 "
					+ "AND ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER + " & (" + Tweets.BUFFER_DISASTER+ "|"+ Tweets.BUFFER_MYDISASTER +") ) !=0 "
					+ "ORDER BY " + Tweets.DEFAULT_SORT_ORDER +";";
			c = database.rawQuery(sql, null);

			// TODO: correct notification URI
			c.setNotificationUri(getContext().getContentResolver(), Tweets.TABLE_FAVORITES_URI);

			// start synch service with a synch favorites request
			i = new Intent(TwitterService.SYNCH_ACTION);
			i.putExtra("synch_request", TwitterService.SYNCH_FAVORITES);
			getContext().startService(i);
			break;

		case TWEETS_FAVORITES_ALL: 
			if (TwimightBaseActivity.D) Log.d(TAG, "Query FAVORITES_ALL");
			sql = "SELECT "
					+ DBOpenHelper.TABLE_TWEETS + "." + "_id AS _id, "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT_PLAIN + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_USER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MENTIONS + ", "					
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_CREATED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_REPLYTO + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FAVORITED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MEDIA + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_HTML_PAGES + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FLAGS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_BUFFER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_DISASTERID + ", "		
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_ISVERIFIED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED_BY + ", "
					+ DBOpenHelper.TABLE_USERS + "." + "_id AS userRowId, "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_SCREENNAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_NAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_PROFILEIMAGE_PATH + " "
					+ "FROM "+DBOpenHelper.TABLE_TWEETS + " "
					+ "JOIN " + DBOpenHelper.TABLE_USERS + " "
					+ "ON " +DBOpenHelper.TABLE_TWEETS+"." +Tweets.COL_SCREENNAME+ "=" +DBOpenHelper.TABLE_USERS+"." +TwitterUsers.COL_SCREENNAME+ " "
					+ "WHERE ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER+"&"+Tweets.BUFFER_FAVORITES+")!=0 "
					+ "ORDER BY " + Tweets.DEFAULT_SORT_ORDER +";";
			c = database.rawQuery(sql, null);

			// TODO: correct notification URI
			c.setNotificationUri(getContext().getContentResolver(), Tweets.TABLE_FAVORITES_URI);

			// start synch service with a synch favorites request
			i = new Intent(TwitterService.SYNCH_ACTION);
			i.putExtra("synch_request", TwitterService.SYNCH_FAVORITES);
			getContext().startService(i);	
			break;

		case TWEETS_MENTIONS_NORMAL: 
			if (TwimightBaseActivity.D) Log.d(TAG, "Query MENTIONS_NORMAL");
			sql = "SELECT "
					+ DBOpenHelper.TABLE_TWEETS + "." + "_id AS _id, "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT_PLAIN + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_USER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MENTIONS + ", "					
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_CREATED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_REPLYTO + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FAVORITED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MEDIA + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_HTML_PAGES + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FLAGS + ", "	
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_BUFFER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_DISASTERID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_ISVERIFIED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED_BY + ", "
					+ DBOpenHelper.TABLE_USERS + "." + "_id AS userRowId, "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_SCREENNAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_NAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_PROFILEIMAGE_PATH + " "
					+ "FROM "+DBOpenHelper.TABLE_TWEETS + " "
					+ "JOIN " + DBOpenHelper.TABLE_USERS + " " 
					+ "ON " +DBOpenHelper.TABLE_TWEETS+"." +Tweets.COL_SCREENNAME+ "=" +DBOpenHelper.TABLE_USERS+"." +TwitterUsers.COL_SCREENNAME+ " "
					+ "WHERE ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER+"&"+Tweets.BUFFER_MENTIONS+")!=0 "
					+ "AND ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER + " & (" + Tweets.BUFFER_DISASTER+ "|"+ Tweets.BUFFER_MYDISASTER +") )=0 "
					+ "ORDER BY " + Tweets.DEFAULT_SORT_ORDER +";";
			c = database.rawQuery(sql, null);

			// TODO: correct notification URI
			c.setNotificationUri(getContext().getContentResolver(), Tweets.TABLE_MENTIONS_URI);

			// start synch service with a synch mentions request
			i = new Intent(TwitterService.SYNCH_ACTION);
			i.putExtra("synch_request", TwitterService.SYNCH_MENTIONS);
			getContext().startService(i);

			break;
		case TWEETS_MENTIONS_DISASTER: 
			if (TwimightBaseActivity.D) Log.d(TAG, "Query MENTIONS_DISASTER");
			sql = "SELECT "
					+ DBOpenHelper.TABLE_TWEETS + "." + "_id AS _id, "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT_PLAIN + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_USER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MENTIONS + ", "					
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_CREATED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_REPLYTO + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FAVORITED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FLAGS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_BUFFER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MEDIA + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_HTML_PAGES + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_DISASTERID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_ISVERIFIED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED_BY + ", "
					+ DBOpenHelper.TABLE_USERS + "." + "_id AS userRowId, "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_SCREENNAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_NAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_PROFILEIMAGE_PATH + " "
					+ "FROM "+DBOpenHelper.TABLE_TWEETS + " "
					+ "JOIN " + DBOpenHelper.TABLE_USERS + " " 
					+ "ON " +DBOpenHelper.TABLE_TWEETS+"." +Tweets.COL_SCREENNAME+ "=" +DBOpenHelper.TABLE_USERS+"." +TwitterUsers.COL_SCREENNAME+ " "
					+ "WHERE ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER+"&"+Tweets.BUFFER_MENTIONS+")!=0 "
					+ "AND ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER + " & (" + Tweets.BUFFER_DISASTER+ "|"+ Tweets.BUFFER_MYDISASTER +") ) !=0 "
					+ "ORDER BY " + Tweets.DEFAULT_SORT_ORDER +";";
			c = database.rawQuery(sql, null);

			// TODO: correct notification URI
			c.setNotificationUri(getContext().getContentResolver(), Tweets.TABLE_MENTIONS_URI);

			// start synch service with a synch mentions request
			i = new Intent(TwitterService.SYNCH_ACTION);
			i.putExtra("synch_request", TwitterService.SYNCH_MENTIONS);
			getContext().startService(i);

			break;
		case TWEETS_MENTIONS_ALL: 
			if (TwimightBaseActivity.D) Log.d(TAG, "Query MENTIONS_ALL");
			sql = "SELECT "
					+ DBOpenHelper.TABLE_TWEETS + "." + "_id AS _id, "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TEXT_PLAIN + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_USER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_TID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MENTIONS + ", "					
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_CREATED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_REPLYTO + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FAVORITED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_FLAGS + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_BUFFER + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_MEDIA + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_HTML_PAGES + ", "				
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_DISASTERID + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_ISVERIFIED + ", "
					+ DBOpenHelper.TABLE_TWEETS + "." +Tweets.COL_RETWEETED_BY + ", "
					+ DBOpenHelper.TABLE_USERS + "." + "_id AS userRowId, "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_SCREENNAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_NAME + ", "
					+ DBOpenHelper.TABLE_USERS + "." +TwitterUsers.COL_PROFILEIMAGE_PATH + " "
					+ "FROM "+DBOpenHelper.TABLE_TWEETS + " "
					+ "JOIN " + DBOpenHelper.TABLE_USERS + " " 
					+ "ON " +DBOpenHelper.TABLE_TWEETS+"." +Tweets.COL_SCREENNAME+ "=" +DBOpenHelper.TABLE_USERS+"." +TwitterUsers.COL_SCREENNAME+ " "
					+ "WHERE ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER+"&"+Tweets.BUFFER_MENTIONS+")!=0 "
					+ "ORDER BY " + Tweets.DEFAULT_SORT_ORDER +";";
			c = database.rawQuery(sql, null);

			// TODO: correct notification URI
			c.setNotificationUri(getContext().getContentResolver(), Tweets.TABLE_MENTIONS_URI);

			// start synch service with a synch mentions request
			i = new Intent(TwitterService.SYNCH_ACTION);
			i.putExtra("synch_request", TwitterService.SYNCH_MENTIONS);
			getContext().startService(i);

			break;	
		default: throw new IllegalArgumentException("Unsupported URI: " + uri);	
		}

		return c;
	}


	/**
	 * Inserts a bunch of tweets into the DB
	 */
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {		
		int numInserted= 0;
		database.beginTransaction();
		try {			

			for (ContentValues value : values){
				if (insertNormalTweet(value) != null) 	
					numInserted++;
			}
			database.setTransactionSuccessful();
		} catch (Exception e) {
			//System.out.println(e);
		} finally {
			database.endTransaction();
		}
		return numInserted;
	}

	/**
	 * Insert a tweet into the DB
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {


		Uri insertUri = null; // the return value;

		switch(tweetUriMatcher.match(uri)){
		case TWEETS_TIMELINE_NORMAL:				

			insertUri = insertNormalTweet(values);
			break;

		case TWEETS_TIMELINE_DISASTER:

			insertUri =  insertDisasterTweet(values);				
			break;

		default: throw new IllegalArgumentException("Unsupported URI: " + uri);	
		}

		return insertUri;
	}

	private Uri insertDisasterTweet(ContentValues values) {

		int disasterId;
		Cursor c;
		Uri insertUri = null; // the return value;		

		// in disaster mode, we set the is disaster flag 
		//and sign the tweet (if we have a certificate for our key pair)

		// if we already have a disaster tweet with the same disaster ID, 
		// we discard the new one
		disasterId = getDisasterID(values);

		c = database.query(DBOpenHelper.TABLE_TWEETS, null, Tweets.COL_DISASTERID+"="+disasterId 
				+ " AND ("+DBOpenHelper.TABLE_TWEETS+"."+Tweets.COL_BUFFER + " & (" + Tweets.BUFFER_DISASTER+ "|"+ Tweets.BUFFER_MYDISASTER +") )!=0 ", null, null, null, null);
		if(c.getCount()>0){
			c.moveToFirst();
			Uri oldUri = Uri.parse("content://"+Tweets.TWEET_AUTHORITY+"/"+Tweets.TWEETS+"/"+Long.toString(c.getLong(c.getColumnIndex("_id"))));			
			return oldUri; 
		}
		c.close();

		CertificateManager cm = new CertificateManager(getContext().getApplicationContext());
		KeyManager km = new KeyManager(getContext().getApplicationContext());		

		//verify whether I was the author or not
		if(LoginActivity.getTwitterId(getContext()).equals(values.getAsInteger(Tweets.COL_USER).toString())){

			signTweet(cm,km,values);

			// if we are in disaster mode, we give the content provider a 
			// second to insert the tweet and then schedule a scanning operation
			if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("prefDisasterMode", false) == true){			
				//new ScanningAlarm(getContext(),0,true);    
				Intent i = new Intent(getContext().getApplicationContext(),ScanningService.class);
				i.putExtra(ScanningService.FORCED_BLUE_SCAN, true);
				getContext().getApplicationContext().startService(i);



			}

		} else {

			verifySignature(cm,km,values);		

			if (ShowTweetListActivity.running==false && 
					PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("prefNotifyTweets", false) == true) {
				// notify user 
				notifyUser(NOTIFY_DISASTER, values.getAsString(Tweets.COL_TEXT));
			}			
		}

		insertUri = insertTweet(values);
		//getContext().getContentResolver().notifyChange(uri, null);
		// delete everything that now falls out of the buffer
		purgeTweets(values);

		return insertUri;
	}







	private void verifySignature(CertificateManager cm, KeyManager km, ContentValues values) {
		String certificate = values.getAsString(Tweets.COL_CERTIFICATE);
		// check validity
		if(cm.checkCertificate(cm.parsePem(certificate), values.getAsLong(Tweets.COL_USER).toString())){

			// check signature
			String signature = values.getAsString(Tweets.COL_SIGNATURE);
			String text = values.getAsString(Tweets.COL_TEXT) + values.getAsString(Tweets.COL_USER);
			if(km.checkSignature(cm.parsePem(certificate), signature, text)){							
				values.put(Tweets.COL_ISVERIFIED, 1);
			} else {
				values.put(Tweets.COL_ISVERIFIED, 0);
			}

		} else {
			values.put(Tweets.COL_ISVERIFIED, 0);
		}

	}



	private void signTweet(CertificateManager cm, KeyManager km, ContentValues values) {
		if(cm.hasCertificate()){
			// we put the signature
			String text = values.getAsString(Tweets.COL_TEXT);
			//Log.i(TAG,"text: "+ text);
			String userId = LoginActivity.getTwitterId(getContext()).toString();

			String signature = km.getSignature(new String(text+userId));			
			values.put(Tweets.COL_SIGNATURE, signature);

			// and the certificate
			values.put(Tweets.COL_CERTIFICATE, cm.getCertificate());

			// and set the is_verified flag to show that the tweet is signed
			values.put(Tweets.COL_ISVERIFIED, 1);
		} else {
			values.put(Tweets.COL_ISVERIFIED, 0);
		}

	}



	private Uri insertNormalTweet(ContentValues values) {

		int disasterId;
		/*
		 *  First, we check if we already have a tweets with the same disaster ID.
		 *  If yes, three cases are possible
		 *  1 If the existing tweet is a disaster tweet, it was uploaded to the server and
		 *    now we receive it from the server. In this case we update the disaster tweet
		 *    accordingly.
		 *  2 If the existing tweet is a tweet of our own which was flagged to insert, the insert
		 *    operation may have been successful but the success was not registered locally.
		 *    In this case we update the new tweet with the new information
		 *  3 It may be a hash function collision (two different tweets have the same hash code)
		 *    Probability of this should be small.  
		 */		
		//disasterId = getDisasterID(values);
		//Cursor c = database.query(DBOpenHelper.TABLE_TWEETS, null, Tweets.COL_DISASTERID+"="+disasterId, null, null, null, null);

		/*System.out.println("Cursor disaster = " + c.getCount());

		if(c.getCount() == 1){   

			c.moveToFirst();
			if(Long.toString(c.getLong(c.getColumnIndex(Tweets.COL_USER))).equals(LoginActivity.getTwitterId(getContext()))) {
				// clear the to insert flag
				int flags = c.getInt(c.getColumnIndex(Tweets.COL_FLAGS));
				values.put(Tweets.COL_FLAGS, flags & (~Tweets.FLAG_TO_INSERT));

			} else if ( values.getAsInteger(Tweets.COL_BUFFER) == Tweets.BUFFER_TIMELINE &&
					(c.getInt(c.getColumnIndex(Tweets.COL_BUFFER)) & Tweets.BUFFER_MENTIONS) == Tweets.BUFFER_MENTIONS ) {


				values.put(Tweets.COL_BUFFER, c.getInt(c.getColumnIndex(Tweets.COL_BUFFER)) | Tweets.BUFFER_TIMELINE);

			} else if ( values.getAsInteger(Tweets.COL_BUFFER) == Tweets.BUFFER_TIMELINE &&
					(c.getInt(c.getColumnIndex(Tweets.COL_BUFFER)) & Tweets.BUFFER_FAVORITES) == Tweets.BUFFER_FAVORITES ) {

				values.put(Tweets.COL_BUFFER, c.getInt(c.getColumnIndex(Tweets.COL_BUFFER)) | Tweets.BUFFER_TIMELINE); 

			} else if( !c.isNull(c.getColumnIndex(Tweets.COL_TID)) &&
					c.getLong(c.getColumnIndex(Tweets.COL_TID))== values.getAsLong(Tweets.COL_TID) ){

				return null;
			}

			Uri updateUri = Uri.parse("content://"+Tweets.TWEET_AUTHORITY+"/"+Tweets.TWEETS+"/"+Integer.toString(c.getInt(c.getColumnIndex("_id"))));
			update(updateUri, values, null, null);
			c.close();

			return updateUri;			
		}
		 */

		// check if the tweet is already in the database and see if we need to update it
		String tweetId = values.getAsString(Tweets.COL_TID);
		System.out.println("Got tweet ID = " + tweetId);
		Cursor c = database.query(DBOpenHelper.TABLE_TWEETS, null, Tweets.COL_TID+"=\""+tweetId + "\"", null, null, null, null);
		if(c.getCount() == 1){
			c.moveToFirst();
			System.out.println("Tweet already in the database...." +  c.getString(c.getColumnIndex(Tweets.COL_TID)));
			System.out.println("Buffer1 = " +  c.getInt(c.getColumnIndex(Tweets.COL_BUFFER)));


			if(c.getString(c.getColumnIndex(Tweets.COL_SCREENNAME)).equals(LoginActivity.getTwitterId(getContext()))) {
				// clear the to insert flag
				int flags = c.getInt(c.getColumnIndex(Tweets.COL_FLAGS));
				values.put(Tweets.COL_FLAGS, flags & (~Tweets.FLAG_TO_INSERT));
			} /*else if ( values.getAsInteger(Tweets.COL_BUFFER) == Tweets.BUFFER_TIMELINE &&
					(c.getInt(c.getColumnIndex(Tweets.COL_BUFFER)) & Tweets.BUFFER_MENTIONS) == Tweets.BUFFER_MENTIONS ) {
				values.put(Tweets.COL_BUFFER, c.getInt(c.getColumnIndex(Tweets.COL_BUFFER)) | Tweets.BUFFER_TIMELINE);
			} else if ( values.getAsInteger(Tweets.COL_BUFFER) == Tweets.BUFFER_TIMELINE &&
					(c.getInt(c.getColumnIndex(Tweets.COL_BUFFER)) & Tweets.BUFFER_FAVORITES) == Tweets.BUFFER_FAVORITES ) {
				values.put(Tweets.COL_BUFFER, c.getInt(c.getColumnIndex(Tweets.COL_BUFFER)) | Tweets.BUFFER_TIMELINE); 
			}*/
			values.put(Tweets.COL_BUFFER, c.getInt(c.getColumnIndex(Tweets.COL_BUFFER)) | values.getAsInteger(Tweets.COL_BUFFER));

			System.out.println("Buffer2 = " +  values.get(Tweets.COL_BUFFER));

			Uri updateUri = Uri.parse("content://"+Tweets.TWEET_AUTHORITY+"/"+Tweets.TWEETS+"/"+Integer.toString(c.getInt(c.getColumnIndex("_id"))));
			update(updateUri, values, null, null);
			c.close();
			return updateUri;
		}

		//this situation happens in case a tweet with media is posted to the servers
		/*c = database.query(DBOpenHelper.TABLE_TWEETS, null, Tweets.COL_TID+"="+values.getAsLong(Tweets.COL_TID), null, null, null, null);
		if(c.getCount() == 1){   
			c.moveToFirst();

			Uri updateUri = Uri.parse("content://"+Tweets.TWEET_AUTHORITY+"/"+Tweets.TWEETS+"/"+Integer.toString(c.getInt(c.getColumnIndex("_id"))));
			update(updateUri, values, null, null);
			c.close();

			return updateUri;		
		}

		c.close();
		 */
		// if none of the before was true, this is a proper new tweet which we now insert
		try {
			System.out.println("Inserting tweet... : " + values.getAsInteger(Tweets.COL_BUFFER));
			Uri insertUri = insertTweet(values);
			if (ShowTweetListActivity.running==false && ( (values.getAsInteger(Tweets.COL_BUFFER) & Tweets.BUFFER_SEARCH) == 0) &&
					PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("prefNotifyTweets", false) == true ) {
				// notify user 				
				notifyUser(NOTIFY_TWEET, values.getAsString(Tweets.COL_TEXT));
			}
			// delete everything that now falls out of the buffer
			purgeTweets(values);
			return insertUri;

		} catch (Exception ex) {
			Log.e(TAG,"exception while inserting", ex);
			return null;
		}


	}

	/**
	 * Update a tweet
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		if(tweetUriMatcher.match(uri) != TWEETS_ID) throw new IllegalArgumentException("Unsupported URI: " + uri);			

		int nrRows = database.update(DBOpenHelper.TABLE_TWEETS, values, "_id="+uri.getLastPathSegment() , null);
		if(nrRows >= 0){
			getContext().getContentResolver().notifyChange(uri, null);
			Log.i(TAG,"updated");
			// Trigger synch if needed
			if(values.containsKey(Tweets.COL_FLAGS) && values.getAsInteger(Tweets.COL_FLAGS)!=0){
				Intent i = new Intent(TwitterService.SYNCH_ACTION);
				i.putExtra("synch_request", TwitterService.SYNCH_TWEET);
				i.putExtra("rowId", new Long(uri.getLastPathSegment()));
				getContext().startService(i);
			}
			return nrRows;
		} else {
			throw new IllegalStateException("Could not update tweet " + values);
		}
	}

	/**
	 * Delete a local tweet from the DB
	 */
	@Override
	public int delete(Uri uri, String arg1, String[] arg2) {
		if(tweetUriMatcher.match(uri) != TWEETS_ID) throw new IllegalArgumentException("Unsupported URI: " + uri);

		Log.d(TAG, "Delete TWEETS_ID");

		int nrRows = database.delete(DBOpenHelper.TABLE_TWEETS, "_id="+uri.getLastPathSegment(), null);
		getContext().getContentResolver().notifyChange(Tweets.TABLE_FAVORITES_URI, null);		
		getContext().getContentResolver().notifyChange(Tweets.TABLE_TIMELINE_URI, null);

		System.out.println("Tweet deleted!");
		
		return nrRows;
	}

	/**
	 * purges a provided buffer to the provided number of tweets
	 */
	private void purgeBuffer(int buffer, int size){
		/*
		 * First, we remove the respective flag
		 * Second, we delete all tweets which have no more buffer flags
		 */
		String sqlWhere;
		String sql;		
		// NOTE: DELETE in android does not allow ORDER BY. Hence, the trick with the _id
		sqlWhere = "("+Tweets.COL_BUFFER+"&"+buffer+")!=0";
		sql = "UPDATE " + DBOpenHelper.TABLE_TWEETS + " "
				+"SET " + Tweets.COL_BUFFER +"=("+(~buffer)+"&"+Tweets.COL_BUFFER+") "
				+"WHERE "
				+"_id IN (SELECT _id FROM "+DBOpenHelper.TABLE_TWEETS 
				+ " WHERE " + sqlWhere
				+ " ORDER BY "+Tweets.DEFAULT_SORT_ORDER+" "
				+ " LIMIT 100 OFFSET "
				+ size +");";		
		database.execSQL(sql);

		// now delete
		int result = database.delete(DBOpenHelper.TABLE_TWEETS, Tweets.COL_BUFFER + "=0" , null);
		Log.d(TAG,"deleted " + result + " tweets");

		getContext().getContentResolver().notifyChange(Tweets.ALL_TWEETS_URI, null);
	}

	/**
	 * Keeps the tweets table at acceptable size
	 */
	private void purgeTweets(ContentValues cv){

		// in the content values we find which buffer(s) to purge
		int bufferFlags = cv.getAsInteger(Tweets.COL_BUFFER);

		if((bufferFlags & Tweets.BUFFER_TIMELINE) != 0){
			Log.d(TAG, "Purging timeline buffer "+ Constants.TIMELINE_BUFFER_SIZE);
			purgeBuffer(Tweets.BUFFER_TIMELINE, Constants.TIMELINE_BUFFER_SIZE);
		}

		if((bufferFlags & Tweets.BUFFER_FAVORITES) != 0){
			Log.d(TAG, "Purging favorites buffer");
			purgeBuffer(Tweets.BUFFER_FAVORITES, Constants.FAVORITES_BUFFER_SIZE);
		}

		if((bufferFlags & Tweets.BUFFER_MENTIONS) != 0){
			Log.d(TAG, "Purging mentions buffer");
			purgeBuffer(Tweets.BUFFER_MENTIONS, Constants.MENTIONS_BUFFER_SIZE);
		}

		if((bufferFlags & Tweets.BUFFER_DISASTER) != 0){
			Log.d(TAG, "Purging disaster buffer");
			purgeBuffer(Tweets.BUFFER_DISASTER, Constants.DTWEET_BUFFER_SIZE);
		}

		if((bufferFlags & Tweets.BUFFER_MYDISASTER) != 0){
			Log.d(TAG, "Purging mydisaster buffer");
			purgeBuffer(Tweets.BUFFER_MYDISASTER, Constants.MYDTWEET_BUFFER_SIZE);
		}

		if((bufferFlags & Tweets.BUFFER_USERS) != 0){
			Log.d(TAG, "Purging user tweets buffer");
			purgeBuffer(Tweets.BUFFER_USERS, Constants.USERTWEETS_BUFFER_SIZE);
		}

		if((bufferFlags & Tweets.BUFFER_SEARCH) != 0){
			Log.d(TAG, "Purging search tweets buffer");
			purgeBuffer(Tweets.BUFFER_SEARCH, Constants.SEARCHTWEETS_BUFFER_SIZE);
		}

	}


	/**
	 * Computes the java String object hash code (32 bit) as the disaster ID of the tweet
	 * TODO: For security reasons (to prevent intentional hash collisions), this should be a cryptographic hash function instead of the string hash.
	 * @param cv
	 * @return
	 */
	private int getDisasterID(ContentValues cv){

		if ( cv != null ) {
			String text = Html.fromHtml(cv.getAsString(Tweets.COL_TEXT), null, null).toString();

			String userId;
			if(!cv.containsKey(Tweets.COL_USER) || (cv.getAsString(Tweets.COL_USER)==null)){
				userId = LoginActivity.getTwitterId(getContext()).toString();
			} else {
				userId = cv.getAsString(Tweets.COL_USER);
			}

			return (new String(text+userId)).hashCode();
		} else 
			return -1;

	}

	/**
	 * Input verification for new tweets
	 * @param values
	 * @return
	 */
	private boolean checkValues(ContentValues values){
		// TODO: Input validation
		return true;
	}

	/**
	 * Inserts a tweet into the DB
	 */
	private Uri insertTweet(ContentValues values){			

		values.put(Tweets.COL_RECEIVED, System.currentTimeMillis());			
		// the disaster ID must be set for all tweets (normal and disaster)
		values.put(Tweets.COL_DISASTERID, getDisasterID(values));			
		// does it mention the local user?
		String text = values.getAsString(Tweets.COL_TEXT);

		localScreenName = LoginActivity.getTwitterScreenname(getContext());
		String localUserScreenName = this.localScreenName;

		if (localUserScreenName != null) {
			// we convert to lower case to check if it's a mention
			if(text.toLowerCase().matches(".*@"+localUserScreenName.toLowerCase()+"\\W.*")){

				values.put(Tweets.COL_MENTIONS, 1);
				// put into mentions buffer
				if(values.containsKey(Tweets.COL_BUFFER)){
					values.put(Tweets.COL_BUFFER, values.getAsInteger(Tweets.COL_BUFFER)|Tweets.BUFFER_MENTIONS);
				} else {
					values.put(Tweets.COL_BUFFER, Tweets.BUFFER_MENTIONS);
				}

				if (ShowTweetListActivity.running==false && 
						PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("prefNotifyMentions", true) == true 
						&& hasBeenExecuted()  ) {
					// notify user						
					notifyUser(NOTIFY_MENTION, values.getAsString(Tweets.COL_TEXT));
				} 

			} else {
				values.put(Tweets.COL_MENTIONS, 0);				
			}
		}		
		try {
			//////////////////////
			//long diff = values.getAsLong(Tweets.COL_RECEIVED) - values.getAsLong(Tweets.COL_CREATED);
			//writeToLog(Long.toString( Math.round( diff/(1000*60) )) );
			//////////////////////////////////

			long rowId = database.insertOrThrow(DBOpenHelper.TABLE_TWEETS, null, values);						
			if(rowId >= 0){							
				Uri insertUri = ContentUris.withAppendedId(Tweets.ALL_TWEETS_URI, rowId);
				return insertUri;
			} else {
				return null; 
			}

		} catch (SQLException ex) {
			Log.e(TAG,"could not insert tweet in the table",ex);
			return null;
		}
	}


	private boolean hasBeenExecuted() {
		return PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(TwitterService.TASK_MENTIONS, false);
	}

	private void writeToLog(String time) {
		// TODO Auto-generated method stub
		try {
			File tempFile = new File("../../../../../sdcard/twimight.tmp");
			FileOutputStream writer;
			//Log.d("LogThread", Environment.getRootDirectory().getAbsolutePath());
			writer = new FileOutputStream(tempFile);
			writer.write(time.getBytes());
			writer.flush();
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Creates and triggers the status bar notifications
	 */
	private void notifyUser(int type, String tickerText){

		NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.ic_launcher_twimight;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, Html.fromHtml(tickerText, null, null), when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Context context = getContext().getApplicationContext();

		CharSequence contentTitle = getContext().getString(R.string.tweet_content_title);
		CharSequence contentText = "New Tweets!";
		Intent notificationIntent = new Intent(getContext(), ShowTweetListActivity.class);
		PendingIntent contentIntent;

		switch(type){
		case(NOTIFY_MENTION):
			contentText = getContext().getString(R.string.mention_content_text);
		notificationIntent.putExtra(ShowTweetListActivity.FILTER_REQUEST, TweetListFragment.MENTIONS_KEY);			
		break;
		case(NOTIFY_DISASTER):
			contentText = getContext().getString(R.string.dis_tweet_content_text);
		notificationIntent.putExtra(ShowTweetListActivity.FILTER_REQUEST, TweetListFragment.FAVORITES_KEY);
		break;
		case(NOTIFY_TWEET):
			contentText = getContext().getString(R.string.tweet_content_text);
		notificationIntent.putExtra(ShowTweetListActivity.FILTER_REQUEST, TweetListFragment.TIMELINE_KEY);
		break;
		default:
			break;
		}

		contentIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(TWEET_NOTIFICATION_ID, notification);
	}

}
