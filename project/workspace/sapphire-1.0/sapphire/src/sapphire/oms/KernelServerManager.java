package sapphire.oms;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;

import sapphire.kernel.server.KernelServer;

/**
 * Manages Sapphire kernel servers. Tracks which servers are up, which regions each server belongs to, etc.
 * @author iyzhang
 *
 */
public class KernelServerManager {
	Logger logger = Logger.getLogger("sapphire.oms.KernelServerManager");

	public static final String[] STANDARD_REGIONS = {"NORTH AMERICA", "SOUTH AMERICA", "ASIA", "EUROPE", "AFRICA", "AUSTRALIA"};
	private int regionIndex;

	private ConcurrentHashMap<InetSocketAddress, KernelServer> servers;
	private ConcurrentHashMap<String, ArrayList<InetSocketAddress>> regions;
	
	private LinkedList<InetSocketAddress> devices;

	public KernelServerManager() throws IOException, NotBoundException, JSONException {
		servers = new ConcurrentHashMap<InetSocketAddress, KernelServer>();
		regions = new ConcurrentHashMap<String, ArrayList<InetSocketAddress>>();
		devices = new LinkedList<InetSocketAddress>();
		regionIndex = 0;
	}
	
	public void registerKernelDeviceServer(InetSocketAddress address) throws RemoteException, NotBoundException {
		synchronized (devices) {
			devices.add(address);
			System.out.println("New kernel device server: " + address.toString());
		}
	}
	
	public ArrayList<InetSocketAddress> getDevices() {
		return new ArrayList<InetSocketAddress>(devices);
	}

	public void registerKernelServer(InetSocketAddress address) throws RemoteException, NotBoundException {
		logger.info("New kernel server: " + address.toString());

		// Randomly generate which region a server belongs to
		String region = STANDARD_REGIONS[regionIndex % STANDARD_REGIONS.length];
		regionIndex++;
		ArrayList<InetSocketAddress> serverAddresses = regions.get(region);
		if (serverAddresses == null) {
			serverAddresses = new ArrayList<InetSocketAddress>();
			regions.put(region, serverAddresses);
		}
		serverAddresses.add(address);
		System.out.println("Added to region: " + region);
	}

	/**
	 */
	public ArrayList<InetSocketAddress> getServers() {
		return new ArrayList<InetSocketAddress>(servers.keySet());
	}

	public ArrayList<String> getRegions() {
		return new ArrayList<String>(regions.keySet());
	}

	public KernelServer getServer(InetSocketAddress address) {
		if (servers.containsKey(address)) {
			return servers.get(address);
		} else {
			KernelServer server = null;
			try {
				/* -- Apache Harmony */
				Registry registry = LocateRegistry.getRegistry(address.getHostName(), address.getPort());
				server = (KernelServer) registry.lookup("SapphireKernelServer");
				/* --- */
				servers.put(address, server);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not find kernel server: "+e.toString());
			}
			return server;
		}
	}
	
	public void setServerToRegion(String region, InetSocketAddress address) {
		String emptyRegion = null;

		// Return if server is already located in the given region
		ArrayList<InetSocketAddress> addresses = regions.get(region);
		if (addresses != null && addresses.contains(address))
			return;
		
		// Remove server from current region
		for (String currentRegion : regions.keySet()) {
			if (!currentRegion.equals(region)) {
				if (regions.get(currentRegion).remove(address)) {
					if (regions.get(currentRegion).isEmpty())
						emptyRegion = currentRegion;
					break;
				} 
			}
		}
		// Add server to given region
		if (addresses == null) {
			addresses = new ArrayList<InetSocketAddress>();
			regions.put(region, addresses);
		}
		addresses.add(address);
		
		// Remove former region from map if region is now empty
		if (emptyRegion != null)
			regions.remove(emptyRegion);
	}

	public InetSocketAddress getServerInRegion(String region) {
		if (regions.get(region) == null) {
			return null;
		} else {
			return regions.get(region).get(0);
		}
	}
}
