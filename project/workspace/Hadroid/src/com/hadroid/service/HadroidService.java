package com.hadroid.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import uw.edu.hadroid.workflow.HadroidFunction;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

import message.HadroidMessage;
import message.RequestTaskMessage;
import message.ResultMessage;
import message.TaskMessage;
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
	private static final String JAR_NAME = "tmp.jar";
	private static final String LOG_TAG = "HadroidService";
	private static final String SERVER_IP = "172.28.7.16";
	//private static final String SERVER_IP = "10.0.2.2";
	private static final int SERVER_PORT = 6669;

	private File dexDir;
	private File tmpFile;
	private Socket serverSocket;

	public HadroidService() {
		super();
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
		super.onCreate();
		Log.d(LOG_TAG, "onCreate");
		Toast.makeText(this, " HadroidService created", Toast.LENGTH_LONG).show();
		dexDir = this.getDir("dex", 0);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(LOG_TAG, "onStart");
		Toast.makeText(this, "HadroidService started", Toast.LENGTH_LONG).show();

		try {
			tmpFile = this.getCacheDir().createTempFile("tmp", ".jar");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(LOG_TAG, "IOException when making temp jar");
		}

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

	// this does all of the actual work!
	class Worker extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				// open the socket to the server
				Log.d(LOG_TAG, "worker executing...");
				serverSocket = new Socket(SERVER_IP, SERVER_PORT);
				serverSocket.setTcpNoDelay(true);

				// open the output stream
				OutputStream outstream = serverSocket.getOutputStream(); 
				ObjectOutputStream oos = new ObjectOutputStream(outstream);
				
				// create a request message
				HadroidMessage msg = new RequestTaskMessage();				
				
				// send the initial request message
				oos.writeObject(msg);
				Log.d(LOG_TAG, "message sent...");

				InputStream in = serverSocket.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(in);

				// keep asking the server for tasks!
				while(true) {
					// get a response
					msg = (HadroidMessage) ois.readObject();
					Log.d(LOG_TAG, "message received: " + msg);


					if (msg instanceof TaskMessage) {
						Log.d(LOG_TAG, "TaskMessage received");
						TaskMessage tm = (TaskMessage)msg;

						// write dex to disk
						FileOutputStream out = new FileOutputStream(tmpFile);
						out.write(tm.getTask().getDexFile());
						out.close();

						// load in the dex file
						DexClassLoader classLoader = new DexClassLoader(tmpFile.getPath(),
								dexDir.getPath(), null, getClass().getClassLoader());
						DexFile df = DexFile.loadDex(tmpFile.getAbsolutePath(),
								new File(dexDir, tmpFile.getName() + ".odex").getAbsolutePath(), 0);

						List results = null;

						// find the HadroidFunction and apply it to the given data
						for (Enumeration<String> iter = df.entries(); iter.hasMoreElements();) {
							String className = iter.nextElement();
							Log.i(LOG_TAG,"Found class: " + className);
							Class<?> cls = Class.forName(className, true, classLoader);
							if (HadroidFunction.class.isAssignableFrom(cls)) {
								// HadroidFunction class found
								Log.i(LOG_TAG, "Found HadroidFunction: " + className);
								HadroidFunction fxn = (HadroidFunction) cls.newInstance();
								results = fxn.run(tm.getTask().getData());

								// only one HadroidFunction should exist
								break;
							}
						}

						// send the results back
						ResultMessage rm = new ResultMessage(tm.getTask().getUuid(), results);
						oos.writeObject(rm);
					}				
				}
			} catch (UnknownHostException e) {
				Log.e(LOG_TAG, "UnknownHostException");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(LOG_TAG, "IOException");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				Log.e(LOG_TAG, "ClassNotFoundException!");
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
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

	class Ticker extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO make a heartbeat signal... need the socket for this
			return null;
		}
	}
}
