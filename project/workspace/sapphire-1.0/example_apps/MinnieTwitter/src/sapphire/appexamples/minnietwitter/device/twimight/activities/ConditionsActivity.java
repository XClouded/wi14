package sapphire.appexamples.minnietwitter.device.twimight.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import sapphire.app.AndroidSapphireActivity;
import sapphire.appexamples.minnietwitter.device.twimight.R;
import sapphire.appexamples.minnietwitter.device.twimight.global.ObjectSerializer;
import sapphire.appexamples.minnietwitter.device.twimight.global.StateSingleton;

public class ConditionsActivity extends Activity {

	static final String TERMS = "termsAccepted";
	//static final String APP_ENTRY_POINT = "appEntryPoint";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ConditionsActivity.this);
		//SharedPreferences.Editor editor = settings.edit();
		boolean termsAccepted = settings.getBoolean(TERMS, false);
		
		if (termsAccepted) {

			//if (settings.getString(APP_ENTRY_POINT, null) == null) {
			//try {
			//	editor.putString(APP_ENTRY_POINT, ObjectSerializer.serialize(getAppEntryPoint()));
			//} catch (IOException e) {
			//		e.printStackTrace();
			//	}
			//}
			startLogin();

		} else {

			setContentView(R.layout.show_conditions);		
			Button buttonAgree = (Button)findViewById(R.id.buttonAgree);

			buttonAgree.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ConditionsActivity.this);
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean(TERMS, true);
					editor.commit();  

					setContentView(R.layout.show_tips);
					Button buttonSkip = (Button)findViewById(R.id.buttonSkip);
					buttonSkip.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							startLogin();

						}


					});

				}			
			});

		}


	}

	private void startLogin() {
		Intent intent = new Intent(this,LoginActivity.class);
		startActivity(intent);
		finish();
	}




}
