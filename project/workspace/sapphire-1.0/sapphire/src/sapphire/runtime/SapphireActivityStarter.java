package sapphire.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import sapphire.app.SapphireActivity;
import sapphire.app.SapphireObject;
import sapphire.kernel.server.KernelServer;
import sapphire.kernel.server.KernelServerImpl;
import sapphire.oms.OMSServer;

/** 
 *   Starts a SapphireActivity
 */

public class SapphireActivityStarter {

	public static void main(String[] args) {
		Registry registry;
		try {
			/* Start the Sapphire Kernel Server */
			InetSocketAddress host, omsHost = null, storageHost = null;
			File file;

			try {
				host = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
				file = new File(args[2]);
			} catch (NumberFormatException e) {
				System.out.println("Incorrect arguments to the kernel server");
				System.out.println("[host ip] [host port] [config file]");
				return;
			}

			System.setProperty("java.rmi.server.hostname", host.getAddress().getHostAddress());

			String jsonString = "";
			try {
				jsonString = fileToString(file);
			} catch (IOException e) {
				System.out.println("Could not read server JSON file: " + e.toString());			
			}

			String storeName = "";
			try {
				JSONTokener t = new JSONTokener(jsonString);
				JSONObject jsonObj = new JSONObject(t);

				JSONObject jsonOMS = jsonObj.getJSONObject("oms");
				JSONObject jsonStorage = jsonObj.getJSONObject("storage");

				omsHost = new InetSocketAddress(jsonOMS.getString("hostname"), jsonOMS.getInt("port"));
				storageHost = new InetSocketAddress(jsonStorage.getString("hostname"), jsonStorage.getInt("port"));
				storeName = jsonStorage.getString("store");
			} catch (JSONException e){
				System.out.println("Could not parse server JSON file: " + e.toString());			
			}

			/* Get a reference to the OMS */
			/* --- Apache Hrmony RMI */
			registry = LocateRegistry.getRegistry(omsHost.getHostName(), omsHost.getPort());
			OMSServer oms = (OMSServer) registry.lookup("SapphireOMS");
			/* --- */

			try {
				KernelServerImpl server = new KernelServerImpl(host, omsHost, storageHost, storeName);
				/* --- Apache Harmony */
				KernelServer stub = (KernelServer) UnicastRemoteObject.exportObject(server, 0);
				registry = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
				registry.rebind("SapphireKernelServer", stub);
				//oms.registerKernelServer(host);
				/* --- */

				System.out.println("Server ready!");

			} catch (Exception e) {
				e.printStackTrace();
			}

			/* Start the activity */
			SapphireActivity activity =  (SapphireActivity) Class.forName(args[3]).newInstance();
			activity.onCreate((SapphireObject) oms.getAppEntryPoint());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String fileToString(File file) throws IOException {
		StringBuffer buf = new StringBuffer();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String str = reader.readLine();
		while (str != null) {
			buf.append(str);
			str = reader.readLine();
		}
		reader.close();
		return buf.toString();
	}
}
