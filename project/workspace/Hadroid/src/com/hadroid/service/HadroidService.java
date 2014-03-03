package com.hadroid.service;

import message.HadroidMessage;
import message.RequestTaskMessage;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Main entry point into the Hadroid service
 * @author jacobs22
 *
 */
public class HadroidService extends Service {
	private static final String workstationIP = "128.208.7.104";
	private static final int SERVER_PORT = 6669;

	public HadroidService() {
		//TODO anything?
	}

	@Override
	public IBinder onBind(Intent i) {
		Log.d("HadroidService", "onBind");
		Bundle args = i.getExtras();
		//TODO do something
		return null;
	}

	@Override
	public void onCreate() {
		Log.d("HadroidService", "onCreate");
		Toast.makeText(this, " HadroidService created", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d("HadroidService", "onStart");
		Toast.makeText(this, "HadroidService started", Toast.LENGTH_LONG).show();
		
		HadroidMessage msg = new RequestTaskMessage();
		
	}

	@Override
	public void onDestroy() {
		Log.d("HadroidService", "onDestroy");
		Toast.makeText(this, "HadroidService destroyed", Toast.LENGTH_LONG).show();
	}
}
