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
import message.PingAliveMessage;
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
//	private static final String SERVER_IP = "172.28.7.16";
	private static final String SERVER_IP = "10.0.2.2";
	private static final int SERVER_PORT = 6669;

	private File dexDir;
	private File tmpFile;
	private Socket workerSocket, tickerSocket;
	private UUID serviceUUID;

	public HadroidService() {
		super();
		workerSocket = null;
		tickerSocket = null;
		serviceUUID = UUID.randomUUID();
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

		Log.d(LOG_TAG,"starting worker...");
		Worker w = new Worker();
		w.execute();
		
//		Log.d(LOG_TAG,"starting ticker...");
//		Ticker t = new Ticker();
//		t.start();
	}

	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "onDestroy");
		Toast.makeText(this, "HadroidService destroyed", Toast.LENGTH_LONG).show();

		if(workerSocket != null) {
			try {
				workerSocket.close();
			} catch (IOException e) {
				// eat the error
			}
			workerSocket = null;
		}

		if(tickerSocket != null) {
			try {
				tickerSocket.close();
			} catch (IOException e) {
				// eat the error
			}
			tickerSocket = null;
		}
	}

	// this does all of the actual work!
	class Worker extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				// open the socket to the server
				Log.d(LOG_TAG, "worker executing...");
				workerSocket = new Socket(SERVER_IP, SERVER_PORT);
				workerSocket.setTcpNoDelay(true);

				// open the output stream
				OutputStream outstream = workerSocket.getOutputStream(); 
				ObjectOutputStream oos = new ObjectOutputStream(outstream);

				// create a request message
				HadroidMessage msg = new RequestTaskMessage(serviceUUID);				

				// send the initial request message
				oos.writeObject(msg);
				Log.d(LOG_TAG, "message sent...");

				InputStream in = workerSocket.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(in);

				// keep asking the server for tasks!
				while(workerSocket != null) {
					// get a response
				    Log.d(LOG_TAG, "waiting for message...");
					msg = (HadroidMessage) ois.readObject();
					Log.d(LOG_TAG, "message received: " + msg);


					if (msg instanceof TaskMessage) {
						Log.d(LOG_TAG, "TaskMessage received");
						TaskMessage tm = (TaskMessage)msg;
						String functionClassName = tm.getTask().getClassName();
						List inputData = tm.getTask().getData();
                        List results = null;

						try{
						    Log.i(LOG_TAG, "function class is found, no need to load from dex");
						    Class cls = Class.forName(functionClassName);
						    results = runFunction(inputData, cls);
						}catch(ClassNotFoundException e){
						    Log.i(LOG_TAG, "function class not found, loading from dex...");
						    // write dex to disk
						    FileOutputStream out = new FileOutputStream(tmpFile);
						    out.write(tm.getTask().getDexFile());
						    out.close();
						    
						    // load in the dex file
						    Log.i(LOG_TAG, "loading dex file");
						    DexClassLoader classLoader = new DexClassLoader(tmpFile.getPath(),
						            dexDir.getPath(), null, getClass().getClassLoader());
						    DexFile df = DexFile.loadDex(tmpFile.getAbsolutePath(),
						            new File(dexDir, tmpFile.getName() + ".odex").getAbsolutePath(), 0);
						    
						    // find the HadroidFunction and apply it to the given data
						    for (Enumeration<String> iter = df.entries(); iter.hasMoreElements();) {
						        String className = iter.nextElement();
						        Log.i(LOG_TAG,"Found class: " + className);
						        Class<?> cls = Class.forName(className, true, classLoader);
						        if (HadroidFunction.class.isAssignableFrom(cls)) {
						            // HadroidFunction class found
						            Log.i(LOG_TAG, "Found HadroidFunction: " + className);
						            results = runFunction(inputData, cls);
						            // only one HadroidFunction should exist
						            break;
						        }
						    }
						}
						
						
						// send the results back
						Log.i(LOG_TAG, "sending back the result");
						ResultMessage rm = new ResultMessage(serviceUUID, tm.getTask().getUuid(), results);
						Log.i(LOG_TAG, "about to send");
						oos.writeObject(rm);
						Log.i(LOG_TAG, "sent");
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
				if (workerSocket != null) {
					try {
						workerSocket.close();
					} catch (IOException e) {
						// I give up
					}
					workerSocket = null;
				}
			}

			return null;
		}

        private List runFunction(List inputData, Class<?> cls)
                throws InstantiationException, IllegalAccessException {
            List results;
            HadroidFunction fxn = (HadroidFunction) cls.newInstance();
            results = fxn.run(inputData);
            return results;
        }

	}

	class Ticker extends Thread {

		@Override
		public void run() {
			Log.d(LOG_TAG, "Ticker started...");
			HadroidMessage ping = new PingAliveMessage(serviceUUID);
			ObjectOutputStream toServer = null;
			try {
				tickerSocket = new Socket(SERVER_IP, SERVER_PORT);
				tickerSocket.setTcpNoDelay(true);

				toServer = new ObjectOutputStream(tickerSocket.getOutputStream());
				
				while (tickerSocket != null) {
					Log.d(LOG_TAG, "Ticker ticking...");
					// send the alive ping
					toServer.writeObject(ping);

					Log.d(LOG_TAG, "Ticker sleeping...");

					// sleep 10 seconds
					Thread.sleep(1*1000);
				}
			} catch (InterruptedException e) {
				Log.e(LOG_TAG, "InterruptedException in Ticker");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(LOG_TAG, "IOException in Ticker");
				e.printStackTrace();
			} finally {
				// free up resources
				if (tickerSocket != null) {
					try {
						tickerSocket.close();
					} catch (IOException e) {
						// I give up
					}
					tickerSocket = null;
				}
				if(toServer != null) {
					try {
						toServer.close();
					} catch (IOException e) {
						// cant do anything
					}
				}
			}
		}
	}
}
