package sapphire.kernel.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import sapphire.app.AppEntryPoint;
import sapphire.common.AppObjectStub;
import sapphire.kernel.client.KernelClient;
import sapphire.kernel.common.GlobalKernelReferences;
import sapphire.kernel.common.KernelOID;
import sapphire.kernel.common.KernelObjectMigratingException;
import sapphire.kernel.common.KernelObjectNotCreatedException;
import sapphire.kernel.common.KernelObjectNotFoundException;
import sapphire.kernel.common.KernelRPCException;
import sapphire.oms.OMSServer;
import sapphire.policy.SapphirePolicy.SapphireServerPolicy;
import sapphire.stats.StatsManager;


/** 
 * Sapphire Kernel Server. Runs on every Sapphire node, knows how to talk to the OMS, handles RPCs and has a client for making RPCs.
 * 
 * @author iyzhang
 *
 */

public class KernelServerImpl implements KernelServer{
	private static Logger logger = Logger.getLogger("sapphire.kernel.server.KernelServerImpl");
	private InetSocketAddress host;
	/** manager for kernel objects that live on this server */
	private KernelObjectManager objectManager;
	/** stub for the OMS */
	public static OMSServer oms;
	/** local kernel client for making RPCs */
	private KernelClient client;
	/** local kernel client to the storage server */
	private KernelStorage store;

	public KernelServerImpl(InetSocketAddress host, InetSocketAddress omsHost, InetSocketAddress storageHost, String storeName) {
		objectManager = new KernelObjectManager();
		Registry registry;
		try {
			/* --- Apache Harmony RMI */
			registry = LocateRegistry.getRegistry(omsHost.getHostName(), omsHost.getPort());
			oms = (OMSServer) registry.lookup("SapphireOMS");
			/* --- */
		} catch (Exception e) {
			logger.severe("Could not find OMS: " + e.toString());
		}

		this.host = host;
		client = new KernelClient(oms);
		GlobalKernelReferences.nodeServer = this;
		GlobalKernelReferences.stats = new StatsManager();
		this.store = new KernelStorage(storageHost, storeName);
	}

	public KernelServerImpl(InetSocketAddress host, InetSocketAddress omsHost, InetSocketAddress storageHost, String storeName, boolean connect_to_store) {
		objectManager = new KernelObjectManager();
		Registry registry;
		try {
			registry = LocateRegistry.getRegistry(omsHost.getHostName(), omsHost.getPort());
			oms = (OMSServer) registry.lookup("SapphireOMS");
		} catch (Exception e) {
			logger.severe("Could not find OMS: " + e.toString());
		}

		this.host = host;
		client = new KernelClient(oms);
		GlobalKernelReferences.nodeServer = this;
		GlobalKernelReferences.stats = new StatsManager();
		if (connect_to_store)
			this.store = new KernelStorage(storageHost, storeName);
	}

	/** RPC INTERFACES **/

	/**
	 * Invoke an RPC on this kernel server. This is a public RMI interface.
	 * 
	 * @param rpc All of the information about the RPC, the object id, the method and arguments
	 * @return the return value from the method invocation
	 */
	@Override
	public Object makeKernelRPC(KernelOID oid, String method, Object[] params) throws RemoteException, KernelObjectNotFoundException, KernelObjectMigratingException, KernelRPCException {
		KernelObject object = null;
		object = objectManager.lookupObject(oid);

		logger.info("Invoking RPC on Kernel Object with OID: " + oid + "with rpc:" + method + " params: " + params);
		Object ret = null;
		try {
			//Stopwatch timer = new Stopwatch();
			ret = object.invoke(method, params);
			//GlobalKernelReferences.stats.logToFile("KernelServerRPC", timer);
		} catch (Exception e) {
			throw new KernelRPCException(e);
		}
		return ret;
	}

	/**
	 * Move a kernel object to this server.
	 * 
	 * @param oid the kernel object id
	 * @param object the kernel object to be stored on this server
	 */
	public void copyKernelObject(KernelOID oid, KernelObject object) throws RemoteException, KernelObjectNotFoundException {
		objectManager.addObject(oid, object);
		object.uncoalesce();
	}

	/** LOCAL INTERFACES **/
	/** 
	 * Create a new kernel object locally on this server.
	 * 
	 * @param stub
	 */
	public KernelOID newKernelObject(Class<?> cl, Object ... args) throws KernelObjectNotCreatedException {
		KernelOID oid = null;
		// get OID
		try {
			oid = oms.registerKernelObject(host);
		} catch (RemoteException e) {
			throw new KernelObjectNotCreatedException("Error making RPC to OMS: "+e);
		}

		// Create the actual kernel object stored in the object manager
		objectManager.newObject(oid, cl, args);
		
		logger.fine("Created new Kernel Object on host: " + host + " with OID: " + oid.getID());
		return oid;
	}

	/**
	 * Move object from this server to host.
	 * @param host
	 * @param oid
	 * @throws RemoteException
	 * @throws KernelObjectNotFoundException
	 */
	public void moveKernelObjectToServer(InetSocketAddress host, KernelOID oid) throws RemoteException, KernelObjectNotFoundException {
		if (host.equals(this.host)) {
			return;
		}

		KernelObject object = objectManager.lookupObject(oid);
		object.coalesce();

		logger.fine("Moving object " + oid.toString() + " to " + host.toString());

		try {
			client.copyObjectToServer(host, oid, object);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RemoteException("Could not contact destination server.");
		}

		try {
			oms.registerKernelObject(oid, host);
		} catch (RemoteException e) {
			throw new RemoteException("Could not contact oms to update kernel object host.");
		}

		objectManager.removeObject(oid);
	}

	public Serializable getObject(KernelOID oid) throws KernelObjectNotFoundException {
		KernelObject object = objectManager.lookupObject(oid);
		return object.getObject();
	}

	/**
	 * Get the local hostname
	 * @return IP address of host that this server is running on
	 */
	public InetSocketAddress getLocalHost() {
		return host;
	}

	/**
	 * Get the kernel client for making RPCs
	 * @return the kernel client in this server
	 */
	public KernelClient getKernelClient() {
		return client;
	}
	
	/**
	 * Get Voldemort client
	 * @return
	 */
	public KernelStorage getKernelStore() {
		return store;
	}
		
	/**
	 * Start the first server-side app object
	 */
	@Override
	public AppObjectStub startApp(String className) throws RemoteException {
		System.out.println("Starting app");
		AppObjectStub appEntryPoint = null;
		try {
			AppEntryPoint entryPoint =  (AppEntryPoint) Class.forName(className).newInstance();
			appEntryPoint = entryPoint.start();
		} catch (Exception e) {
			logger.severe("Could not start app");
			e.printStackTrace();
		}
		return appEntryPoint;
	}

	public class MemoryStatThread extends Thread {
		public void run() {
			while (true) {
				try {
					Thread.sleep(100000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("Total memory: " + Runtime.getRuntime().totalMemory() + " Bytes");
				System.out.println("Free memory: " + Runtime.getRuntime().freeMemory() + " Bytes");
			}
		}
	}

	public MemoryStatThread getMemoryStatThread() {
		return new MemoryStatThread();
	}

	static private String fileToString(File file) throws IOException {
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
	
	public void registerLatencyCallback(KernelOID oid, long maxLatency, SapphireServerPolicy serverPolicy) {
		try {
			KernelObject obj = objectManager.lookupObject(oid);
			obj.registerLatencyCallback(maxLatency, serverPolicy);
		} catch (KernelObjectNotFoundException e) {
			System.err.println("Failed to register latency callback for kernel object (" + oid.getID() + ")");
		}
	}

	/**
	 * At startup, contact the OMS.
	 * @param args
	 */
	public static void main(String args[]) {

		if (args.length != 3) {
			System.out.println("Incorrect arguments to the kernel server");
			System.out.println("[host ip] [host port] [config file]");
			return;
		}

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
			System.out.println("Could not read server JSON file: "+e.toString());
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
			System.out.println("Could not parse server JSON file: "+e.toString());
		}

		try {
			KernelServerImpl server = new KernelServerImpl(host, omsHost, storageHost, storeName);
			/* --- Apache Harmony */
			KernelServer stub = (KernelServer) UnicastRemoteObject.exportObject(server, 0);
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
			registry.rebind("SapphireKernelServer", stub);
			/* --- */

			oms.registerKernelServer(host);

			System.out.println("Server ready!");

			/* Start a thread that prints memory stats */
			//server.getMemoryStatThread().start();

		} catch (Exception e) {
			logger.severe("Cannot start Sapphire Kernel Server");
			e.printStackTrace();
		}
	}
}
