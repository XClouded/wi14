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
package sapphire.appexamples.minnietwitter.device.twimight.activities;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import sapphire.appexamples.minnietwitter.device.twimight.R;
import sapphire.appexamples.minnietwitter.device.twimight.fragments.TweetListFragment;

/**
 * Shows the most recent tweets of a user
 * @author thossmann
 *
 */
public class ShowUserTweetListActivity extends TwimightBaseActivity{

	private static final String TAG = "ShowUserTweetListActivity";	
	
	/** 
	 * Called when the activity is first created. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		System.out.println("In ShowUserTweetListActivity");
		
		if(!getIntent().hasExtra("screenname")) finish();
		
		setContentView(R.layout.main);
				
		String screenname = getIntent().getStringExtra("screenname");
		FragmentManager fragmentManager = getFragmentManager();
	    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        TweetListFragment tlf = new TweetListFragment(TweetListFragment.USER_TWEETS);
        
        Bundle bundle =  new Bundle();
        bundle.putString(TweetListFragment.USER_ID, screenname);
        tlf.setArguments(bundle);
        
        fragmentTransaction.add(R.id.rootRelativeLayout,tlf);
        fragmentTransaction.commit();
		
	}
	
	

	
	
	
	
}
