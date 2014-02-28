package sapphire.policy.replicate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import sapphire.common.AppObject;
import sapphire.kernel.common.GlobalKernelReferences;
import sapphire.policy.SapphirePolicy;
import sapphire.stats.Stopwatch;

public class MasterSlavePolicy extends SapphirePolicy {
		
	public static class MasterSlaveClientPolicy extends SapphireClientPolicy {
		private static final long serialVersionUID = 1L;
		public MasterSlaveGroupPolicy group;
		public MasterSlaveServerPolicy server;
		
		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (MasterSlaveGroupPolicy)group;
			System.out.println("In onCreate Client Policy");
		}

		@Override
		public void setServer(SapphireServerPolicy server) {
			this.server = (MasterSlaveServerPolicy) server;
		}

		@Override
		public SapphireServerPolicy getServer() {
			return server;
		}

		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}
		
		@Override
		public Object onRPC(String method, Object[] params) throws Exception {
			Object ret = server.onRPC(method, params);
			return ret;
		}
	}
	
	public static class MasterSlaveServerPolicy extends SapphireServerPolicy {
		private static final long serialVersionUID = 1L;
		public MasterSlaveGroupPolicy group;
		public MasterSlaveServerPolicy master;
		public ArrayList<MasterSlaveServerPolicy> replicas;
		private boolean isMaster;
		
		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.replicas = new ArrayList<MasterSlaveServerPolicy>();
			this.group = (MasterSlaveGroupPolicy) group;
			this.isMaster = false;
		}

		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}
		
		@Override
		public synchronized Object onRPC(String method, Object[] params) throws Exception {
			// make test RPC
			System.out.println("In onRpc server - isMaster? " + isMaster + ", method = " + method);
			byte[] preObj = objectToByteArray();
			Object obj = appObject.invoke(method, params);
			byte[] postObj = objectToByteArray();
			boolean equal = Arrays.equals(preObj, postObj);
			System.out.println("pre and post objects equal?" + equal);
			if (!equal) {
				if (isMaster) {
					System.out.println("master about to update replicas, # replicas = " + replicas.size());
					for (MasterSlaveServerPolicy replica : replicas) {
						replica.update(postObj);
					}
				} else {
					obj =  master.onRPC(method, params);
				}
			}
			return obj;
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

		@Override
		public void onMembershipChange() { }
		
		public void setAsMaster() {
			isMaster = true;
		}
		
		public void setMasterServer(MasterSlaveServerPolicy master) {
			this.master = master;
		}
		
		public MasterSlaveServerPolicy replicate() {
			return (MasterSlaveServerPolicy) sapphire_replicate();
		}
		
		public AppObject getAppObject() {
			return sapphire_getAppObject();
		}
		
		public void addReplica(MasterSlaveServerPolicy replica) {
			replicas.add(replica);
		}
	}
	
	public static class MasterSlaveGroupPolicy extends SapphireGroupPolicy {
		private static final long serialVersionUID = 1L;
		protected static final int MAX_REPLICAS = 3;
		protected ArrayList<MasterSlaveServerPolicy> servers = new ArrayList<MasterSlaveServerPolicy>();

		@Override
		public void onCreate(SapphireServerPolicy server) {
			servers.add((MasterSlaveServerPolicy) server);
			
			// default: set first server as the master
			((MasterSlaveServerPolicy) server).setAsMaster();
			
			for (int i = 0; i < MAX_REPLICAS; ++i) {
				MasterSlaveServerPolicy master = servers.get(0);
				Stopwatch timer = new Stopwatch();
				MasterSlaveServerPolicy newServer = master.replicate();
				GlobalKernelReferences.stats.log("Replicating master", timer);
				newServer.setMasterServer(master);
				master.addReplica(newServer);
				servers.add(newServer);
			}
		}

		@Override
		public void addServer(SapphireServerPolicy server) {
			//servers.add((SimpleReplicateServerPolicy) server);
		}

		@Override
		public ArrayList<SapphireServerPolicy> getServers() {
			return new ArrayList<SapphireServerPolicy>(servers);
		}

		@Override
		public void onFailure(SapphireServerPolicy server) { }
	}
}


