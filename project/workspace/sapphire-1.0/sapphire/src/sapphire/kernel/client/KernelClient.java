package sapphire.kernel.client;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Hashtable;
import java.util.logging.Logger;

import sapphire.kernel.common.GlobalKernelReferences;
import sapphire.kernel.common.KernelOID;
import sapphire.kernel.common.KernelObjectMigratingException;
import sapphire.kernel.common.KernelObjectNotAvailableException;
import sapphire.kernel.common.KernelObjectNotFoundException;
import sapphire.kernel.common.KernelObjectStub;
import sapphire.kernel.common.KernelRPCException;
import sapphire.kernel.server.KernelObject;
import sapphire.kernel.server.KernelServer;
import sapphire.oms.OMSServer;
import sapphire.stats.Stopwatch;

/** 
 * Client-side object for making Sapphire kernel RPCs.
 * 
 * @author iyzhang
 *
 */
public class KernelClient {
	/** Stub for the OMS */
	private OMSServer oms;
	/** List of hostnames matched to kernel server stubs */
	private Hashtable<InetSocketAddress,KernelServer> servers;
	private Logger logger = Logger.getLogger(KernelClient.class.getName());

	/** 
	 * Add a host to the list of hosts that we've contacted
	 * 
	 * @param hostname
	 */
	private KernelServer addHost(InetSocketAddress host) {
		try {
			/* --- Apache Harmony */
			Registry registry = LocateRegistry.getRegistry(host.getHostName(), host.getPort());
			KernelServer server = (KernelServer) registry.lookup("SapphireKernelServer");
			/* --- */

			servers.put(host, server);
			return server;
		} catch (Exception e) {
			logger.severe("Could not find Sapphire server on host: " + e.toString());
			e.printStackTrace();
		}
		return null;
	}

	private KernelServer getServer(InetSocketAddress host) {
		KernelServer server = servers.get(host);
		if (server == null) {
			server = addHost(host);
		}
		return server;
	}

	public KernelClient(OMSServer oms) {
		this.oms = oms;
		servers = new Hashtable<InetSocketAddress,KernelServer>();
	}

	private Object tryMakeKernelRPC(KernelServer server, KernelOID oid, String method, Object[] params) throws KernelObjectNotFoundException, Exception {
		Object ret = null;
		try {
			ret = server.makeKernelRPC(oid, method, params);
		} catch (KernelRPCException e) {
			throw e.getException();
		} catch (KernelObjectMigratingException e) {
			Thread.sleep(100);
			throw new KernelObjectNotAvailableException("Kernel object was migrating. Try again later.");
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new KernelObjectNotAvailableException("Can't call kernel object");
		}
		return ret;
	}

	private Object lookupAndTryMakeKernelRPC(KernelObjectStub stub, KernelOID oid, String method, Object[] params) throws KernelObjectNotFoundException, Exception {
		InetSocketAddress host, oldHost = stub.$__getHostname();

		try {
			host = oms.lookupKernelObject(stub.$__getKernelOID());
		} catch (RemoteException e) {
			throw new KernelObjectNotFoundException("Could not find oms.");
		} catch (KernelObjectNotFoundException e) {
			throw new KernelObjectNotFoundException("This object does not exist!");
		}

		if (host.equals(oldHost)) {
			throw new KernelObjectNotFoundException("Kernel object should be on the server!");
		}

		stub.$__updateHostname(host);
		return tryMakeKernelRPC(getServer(host), oid, method, params);
	}

	/** 
	 * Make an RPC to the kernel server.
	 * 
	 * @param hostname
	 * @param rpc
	 * @return
	 * @throws RemoteException when kernel server cannot be contacted
	 * @throws KernelObjectNotFoundException when kernel server cannot find object
	 */
	public Object makeKernelRPC(KernelObjectStub stub, KernelOID oid, String method, Object[] params) throws KernelObjectNotFoundException, KernelObjectNotAvailableException, Exception {
		Stopwatch timer = new Stopwatch();
		InetSocketAddress host = stub.$__getHostname();
		//logger.info("Making RPC to " + host.toString() + " RPC: " + rpc.toString());

		// Check whether this object is local. TODO: local to VM => also check port... ?
		KernelServer server;
		if (host.equals(GlobalKernelReferences.nodeServer.getLocalHost())) {
			server = GlobalKernelReferences.nodeServer;
		} else {
			server = getServer(host);
		}

		Object ret;
		// Call the server
		try {
			ret = tryMakeKernelRPC(server, oid, method, params);
		} catch (KernelObjectNotFoundException e) {
			ret = lookupAndTryMakeKernelRPC(stub, oid, method, params);
		} catch (KernelObjectNotAvailableException e) {
			ret = lookupAndTryMakeKernelRPC(stub, oid, method, params);
		}

		//GlobalKernelReferences.stats.logToFile("KernelClientRPC", timer);
		return ret;
	}

	public void copyObjectToServer(InetSocketAddress host, KernelOID oid, KernelObject object) throws RemoteException, KernelObjectNotFoundException {
		getServer(host).copyKernelObject(oid, object);
	}
}
