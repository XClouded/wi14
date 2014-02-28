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
package sapphire.appexamples.minnietwitter.device.twimight.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import sapphire.appexamples.minnietwitter.device.twimight.R;
import sapphire.appexamples.minnietwitter.device.twimight.activities.LoginActivity;
import sapphire.appexamples.minnietwitter.device.twimight.net.Html.StartServiceHelper;
import sapphire.appexamples.minnietwitter.device.twimight.net.opportunistic.ScanningAlarm;
import sapphire.appexamples.minnietwitter.device.twimight.net.tds.TDSAlarm;
import sapphire.appexamples.minnietwitter.device.twimight.net.twitter.TwitterAlarm;

/**
 * Starts the updater service after the boot process.
 * @author pcarta
 * @author thossmann
 *
 */
public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = "BootBroadcastReceiver";
	
	/**
	 * Starts the twimight services upon receiving a boot Intent.
	 * @param context
	 * @param intent
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		// we only start the services if we are logged in (i.e., we have the tokens from twitter)
		if(LoginActivity.hasAccessToken(context) && LoginActivity.hasAccessTokenSecret(context)){

			StartServiceHelper.startService(context);

			// Start the service for communication with the TDS
			if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.prefTDSCommunication), 
					Constants.TDS_DEFAULT_ON)==true){
				new TDSAlarm(context, Constants.TDS_UPDATE_INTERVAL);
			}


			if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.prefDisasterMode), Constants.DISASTER_DEFAULT_ON)==true){
				new ScanningAlarm(context,false);
			}

						
			if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.prefRunAtBoot), Constants.TWEET_DEFAULT_RUN_AT_BOOT)==true){
				new TwitterAlarm(context,false);
			}
		}

	}
	
}
