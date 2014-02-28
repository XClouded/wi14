package sapphire.policy.scalability;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import sapphire.kernel.common.GlobalKernelReferences;
import sapphire.policy.DefaultSapphirePolicy;

public class LoadBalancedMasterSlave extends DefaultSapphirePolicy {

	public static class MasterSlaveClientPolicy extends DefaultClientPolicy {
		private static final long serialVersionUID = 1L;
		public LoadBalancedMasterSlaveGroupPolicy group;
		public LoadBalancedMasterSlaveServerPolicy server;

		@Override
		public Object onRPC(String method, Object[] params) throws Exception {
			// Keep trying until we have an un-overloaded server
			while (true) {
				try {
					return super.onRPC(method, params);
				} catch (ObjectOverloadedException e) {
					server = (LoadBalancedMasterSlaveServerPolicy) group.onRefRequest();
				}
			}
		}
	}

	public static class LoadBalancedMasterSlaveServerPolicy extends DefaultServerPolicy {
		private static final long serialVersionUID = 1L;
		public LoadBalancedMasterSlaveGroupPolicy group;
		public LoadBalancedMasterSlaveServerPolicy master;
		public ArrayList<LoadBalancedMasterSlaveServerPolicy> replicas;
		private boolean isMaster;
		public long duration = 1000000; //duration between calls estimate measured in nano-seconds (default to 1 second)
		public static final double loadThreshold = 1.2/10000;
		public static final double learnRate = 0.1;
		public long lastCall = System.nanoTime();
		private String lock = new String("lock");

		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.replicas = new ArrayList<LoadBalancedMasterSlaveServerPolicy>();
			this.group = (LoadBalancedMasterSlaveGroupPolicy) group;
			this.isMaster = false;
		}

		public synchronized void updateReplicas(byte[] postObj) {
			final byte[] po = postObj;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					for (LoadBalancedMasterSlaveServerPolicy replica : replicas) {
						replica.update(po);
					}
				}
			});
		}

		@Override
		public synchronized Object onRPC(String method, Object[] params) throws Exception {
			synchronized(lock) {
				long now = System.nanoTime();
				duration = (long) ((1-learnRate) * duration + learnRate * (now-lastCall));
				lastCall = now;

				if(this.load() > loadThreshold) {
					duration = (long) 1.50 * duration;
					throw new ObjectOverloadedException();
				}
			}

			byte[] preObj = objectToByteArray();
			Object obj = appObject.invoke(method, params);
			byte[] postObj = objectToByteArray();
			boolean equal = Arrays.equals(preObj, postObj);
			if (!equal) {
				if (isMaster) {
					updateReplicas(postObj);
				} else {
					obj =  master.onRPC(method, params);
				}
			}
			return obj;
		}

		public double load() {
			return 1.0/duration;
		}

		public void update(byte[] obj) {
			Serializable object = (Serializable) byteArrayToObject(obj);
			appObject.setObject(object);
		}

		public Object byteArrayToObject(byte[] b) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(b);
				ObjectInputStream ois = new ObjectInputStream(bais);
				return ois.readObject();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}

		public byte[] objectToByteArray() {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(appObject.getObject());
				return baos.toByteArray();
			} catch (IOException e) {
				return null;
			}
		}

		public void setAsMaster() {
			isMaster = true;
		}

		public void setMasterServer(LoadBalancedMasterSlaveServerPolicy master) {
			this.master = master;
		}

		public LoadBalancedMasterSlaveServerPolicy replicate() {
			return (LoadBalancedMasterSlaveServerPolicy) sapphire_replicate();
		}

		public void addReplica(LoadBalancedMasterSlaveServerPolicy replica) {
			replicas.add(replica);
		}

		public void pin(InetSocketAddress address) {
			try {
				sapphire_pin(address);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public static class LoadBalancedMasterSlaveGroupPolicy extends DefaultGroupPolicy {
		private static final long serialVersionUID = 1L;
		protected static final int MAX_REPLICAS = 3;
		private InetSocketAddress masterAddress;
		public static final double loadThreshold = 1.2/10000;
		private final Map<InetSocketAddress, LoadBalancedMasterSlaveServerPolicy> replicaMap = 
				new Hashtable<InetSocketAddress, LoadBalancedMasterSlaveServerPolicy>();

		@Override
		public void onCreate(SapphireServerPolicy server) {
			// Set first server as the master
			masterAddress = GlobalKernelReferences.nodeServer.getLocalHost();
			replicaMap.put(masterAddress, (LoadBalancedMasterSlaveServerPolicy) server);
			((LoadBalancedMasterSlaveServerPolicy) server).setAsMaster();
		}

		@Override
		public synchronized SapphireServerPolicy onRefRequest() {
			ArrayList<LoadBalancedMasterSlaveServerPolicy> servers = (ArrayList<LoadBalancedMasterSlaveServerPolicy>) replicaMap.values();

			int minInd = 0;
			double minLoad = Double.MAX_VALUE;
			for(int i = 0; i < servers.size(); i++) {
				double cur = servers.get(i).load();
				if(cur < minLoad) {
					minLoad = cur;
					minInd = i;
				}
			}

			if (minLoad < loadThreshold || servers.size() == MAX_REPLICAS) {
				return servers.get(minInd);
			}

			// If we don't have a free enough server, we add a new replica
			// TODO: we need a more elaborate way to iterate over servers, in the OMS
			ArrayList<String> regions;
			try {
				regions = sapphire_getRegions();

				for (String region : regions) {
					InetSocketAddress node = sapphire_getServerInRegion(region);
					LoadBalancedMasterSlaveServerPolicy server = replicaMap.get(node);
					if (server == null) {
						// Create new server policy replica and pin to client
						LoadBalancedMasterSlaveServerPolicy master = (LoadBalancedMasterSlaveServerPolicy) replicaMap.get(masterAddress);
						LoadBalancedMasterSlaveServerPolicy newServer = (LoadBalancedMasterSlaveServerPolicy) master.replicate();
						newServer.pin(node);
						newServer.setMasterServer(master);
						master.addReplica(newServer);
						replicaMap.put(node, newServer);
						return newServer;
					}
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}
	}
}

