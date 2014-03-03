package com.hadroid.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import message.HadroidMessage;
import message.RequestTaskMessage;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
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
	private static final String LOG_TAG = "HadroidService";
	//private static final String SERVER_IP = "172.28.7.96";
	private static final String SERVER_IP = "169.254.33.128";
	private static final int SERVER_PORT = 6669;
	private Socket serverSocket;

	public HadroidService() {
		//TODO anything?
		serverSocket = null;
	}

	@Override
	public IBinder onBind(Intent i) {
		Log.d(LOG_TAG, "onBind");
		Bundle args = i.getExtras();
		//TODO do something
		return null;
	}

	@Override
	public void onCreate() {
		Log.d(LOG_TAG, "onCreate");
		Toast.makeText(this, " HadroidService created", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(LOG_TAG, "onStart");
		Toast.makeText(this, "HadroidService started", Toast.LENGTH_LONG).show();
		Worker w = new Worker();
		w.execute();
	}

	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "onDestroy");
		Toast.makeText(this, "HadroidService destroyed", Toast.LENGTH_LONG).show();
		
		if(serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// eat the error
			}
			serverSocket = null;
		}
	}
	
	class Worker extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				// open the socket to the server
				Log.d(LOG_TAG, "worker executing...");
				serverSocket = new Socket(SERVER_IP, SERVER_PORT);
				serverSocket.setTcpNoDelay(true);
				
				// create a request message
				HadroidMessage msg = new RequestTaskMessage();
				
				// send the message
				OutputStream outstream = serverSocket.getOutputStream(); 
				ObjectOutputStream oos = new ObjectOutputStream(outstream);
				oos.writeObject(new RequestTaskMessage());
				Log.d(LOG_TAG, "message sent...");
				
				// get a response
				InputStream in = serverSocket.getInputStream();
	            ObjectInputStream ois = new ObjectInputStream(in);
	            msg = (HadroidMessage) ois.readObject();
	            Log.d(LOG_TAG, "message received: " + msg);
				
			} catch (UnknownHostException e) {
				Log.e(LOG_TAG, "UnknownHostException");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(LOG_TAG, "IOException");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				Log.e(LOG_TAG, "ClassNotFoundException!");
				e.printStackTrace();
			} finally {
				// close the socket
				if (serverSocket != null) {
					try {
						serverSocket.close();
					} catch (IOException e) {
						// I give up
					}
					serverSocket = null;
				}
			}
			
			return null;
		}
		
	}
}
