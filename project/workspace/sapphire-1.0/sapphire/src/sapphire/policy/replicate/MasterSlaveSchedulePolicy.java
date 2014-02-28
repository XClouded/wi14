package sapphire.policy.replicate;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import sapphire.kernel.common.GlobalKernelReferences;

public class MasterSlaveSchedulePolicy extends MasterSlavePolicy {
	public static class MasterSlaveScheduleClientPolicy extends MasterSlaveClientPolicy {
		private static final long serialVersionUID = 1L;
		private transient boolean isRegistered = false;
		
		@Override
		public Object onRPC(String method, Object[] params) throws Exception {
			if (!isRegistered) {
				MasterSlaveScheduleServerPolicy localServer = ((MasterSlaveScheduleGroupPolicy)group).registerClient(
						GlobalKernelReferences.nodeServer.getLocalHost());
				setServer(localServer);
				isRegistered = true;
			}
			return super.onRPC(method, params);
		}
	}
	
	public static class MasterSlaveScheduleServerPolicy extends MasterSlaveServerPolicy {
		private static final long serialVersionUID = 1L;
		
		public void pin(InetSocketAddress address) {
			try {
				sapphire_pin(address);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class MasterSlaveScheduleGroupPolicy extends MasterSlaveGroupPolicy {
		private static final long serialVersionUID = 1L;
		private InetSocketAddress masterAddress;
		private final Map<InetSocketAddress, MasterSlaveScheduleServerPolicy> replicaMap = 
				new Hashtable<InetSocketAddress, MasterSlaveScheduleServerPolicy>();

		@Override
		public void onCreate(SapphireServerPolicy server) {
			// Set first server as the master
			masterAddress = GlobalKernelReferences.nodeServer.getLocalHost();
			replicaMap.put(masterAddress, (MasterSlaveScheduleServerPolicy) server);
			((MasterSlaveScheduleServerPolicy) server).setAsMaster();
		}
		
		public synchronized MasterSlaveScheduleServerPolicy registerClient(InetSocketAddress clientAddress) {
			MasterSlaveScheduleServerPolicy localServer = replicaMap.get(clientAddress);
			if (localServer != null) {
				// Local server policy already exists
				return localServer;
			} else {
				// Create new server policy replica and pin to client
				MasterSlaveScheduleServerPolicy master = (MasterSlaveScheduleServerPolicy) replicaMap.get(masterAddress);
				MasterSlaveScheduleServerPolicy newServer = (MasterSlaveScheduleServerPolicy) master.replicate();
				newServer.pin(clientAddress);
				newServer.setMasterServer(master);
				master.addReplica(newServer);
				replicaMap.put(clientAddress, newServer);
				return newServer;
			}
			// TODO: return existing server policy if there are too many replicas
		}
		
		@Override
		public ArrayList<SapphireServerPolicy> getServers() {
			return new ArrayList<SapphireServerPolicy>(replicaMap.values());
		}
	}
}
