package sapphire.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import sapphire.common.AppObjectStub;
import sapphire.kernel.server.KernelServer;
import sapphire.kernel.server.KernelServerImpl;
import sapphire.oms.OMSServer;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class AndroidSapphireActivity extends Activity {
	AppObjectStub appEntryPoint = null;
	OMSServer oms = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public AppObjectStub getAppEntryPoint() {
		if (appEntryPoint != null)
			return appEntryPoint;
		else {
			Registry registry;
			try {
				/* Start the Sapphire Kernel Server */
				InetSocketAddress host, omsHost = null, storageHost = null;
				InputStream ims;
				int port = -1;

				String jsonString = "";
				try {
					ims = getAssets().open("config.json");
					jsonString = fileToString(ims);
				} catch (IOException e) {
					System.out.println("Could not read server JSON file: " + e.toString());
				}

				System.out.println("JSONString = " + jsonString);

				String storeName = "";
				try {
					JSONTokener t = new JSONTokener(jsonString);
					JSONObject jsonObj = new JSONObject(t);

					JSONObject jsonOMS = jsonObj.getJSONObject("oms");
					JSONObject jsonStorage = jsonObj.getJSONObject("storage");
					JSONObject jsonClient = jsonObj.getJSONObject("client");

					omsHost = new InetSocketAddress(jsonOMS.getString("hostname"), jsonOMS.getInt("port"));
					storageHost = new InetSocketAddress(jsonStorage.getString("hostname"), jsonStorage.getInt("port"));
					storeName = jsonStorage.getString("store");
					port = jsonClient.getInt("port");
				} catch (JSONException e){
					System.out.println("Could not parse server JSON file: " + e.toString());
				}

				System.out.println("My local IP address: " + getLocalIpAddress());
				host = new InetSocketAddress(getLocalIpAddress(), port);

				System.setProperty("java.rmi.server.hostname", host.getAddress().getHostAddress());

				/* Get a reference to the OMS */
				registry = LocateRegistry.getRegistry(omsHost.getHostName(), omsHost.getPort());
				oms = (OMSServer) registry.lookup("SapphireOMS");

				try {
					KernelServerImpl server = new KernelServerImpl(host, omsHost, storageHost, storeName, false);
					KernelServer stub = (KernelServer) UnicastRemoteObject.exportObject(server, 0);
					registry = LocateRegistry.createRegistry(port);
					registry.rebind("SapphireKernelServer", stub);
					oms.registerKernelDeviceServer(host);
					System.out.println("Server ready!");
				} catch (Exception e) {
					e.printStackTrace();
				}

				/* Get the appEntryPoint */
				appEntryPoint = oms.getAppEntryPoint();
				System.out.println("Got the appEntryPoint: " + appEntryPoint);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return appEntryPoint;
		}
	}

	private String fileToString(InputStream is) throws IOException {
		StringBuffer buf = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String str = reader.readLine();
		while (str != null) {
			buf.append(str);
			str = reader.readLine();
		}
		reader.close();
		return buf.toString();
	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			System.err.println("Can't get my local IP address");
		}
		return null;
	}
}
