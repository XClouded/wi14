package com.example.hadroid;

import com.hadroid.service.HadroidService;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	// Start the  service
	public void startHadroidService(View view) {
		startService(new Intent(this, HadroidService.class));
	}
	// Stop the  service
	public void stopHadroidService(View view) {
		stopService(new Intent(this, HadroidService.class));
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}    
}
