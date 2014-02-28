package sapphire.policy.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import sapphire.common.AppObject;
import sapphire.common.SapphireObjectNotAvailableException;
import sapphire.kernel.common.KernelObjectNotFoundException;
import sapphire.policy.SapphirePolicy;
import sapphire.policy.SapphirePolicy.SapphireClientPolicy;
import sapphire.policy.SapphirePolicy.SapphireGroupPolicy;
import sapphire.policy.SapphirePolicy.SapphireServerPolicy;

/**
 * A caching policy between the mobile device and the server that uses leases for writing.
 * @author iyzhang
 *
 */
public class ExplicitCachePolicy extends SapphirePolicy {
	
	public static class ExplicitCacheClientPolicy extends SapphireClientPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private ExplicitCacheServerPolicy server;
		private ExplicitCacheGroupPolicy group;
		private AppObject cachedObject = null;

		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (ExplicitCacheGroupPolicy) group;
		}

		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}

		@Override
		public SapphireServerPolicy getServer() {
			return server;
		}

		@Override
		public void setServer(SapphireServerPolicy server) {
			this.server = (ExplicitCacheServerPolicy) server;
		}

		private void cache() {
			try {
				cachedObject = server.getObject();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void sync() {
			server.syncObject(cachedObject.getObject());
		}

		/*
 		private Object byteArrayToObject(byte[] b) {
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

		private byte[] objectToByteArray() {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(cachedObject);
				return baos.toByteArray();
			} catch (IOException e) {
				return null;
			}
		}
		*/

		@Override
		public Object onRPC(String method, Object[] params) throws Exception {
			Object ret = null;
			if (cachedObject != null) {
				//byte[] preObj = objectToByteArray(); //invoke on copy?
				ret = cachedObject.invoke(method, params);
				//byte[] postObj = objectToByteArray();
				//boolean equal = Arrays.equals(preObj, postObj);
				//if (!equal) {
				//	ret = super.onRPC(method, params); //get new object and replace?
				//}
			} else {
				ret = super.onRPC(method, params);
			}
			return ret;
		}

		private void writeObject(ObjectOutputStream out) throws IOException {
			 out.writeObject(server);
			 out.writeObject(group);
		 }
	     
		 private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			 server = (ExplicitCacheServerPolicy) in.readObject();
			 group = (ExplicitCacheGroupPolicy) in.readObject();
			 cachedObject = null;
		 }
	}

	public static class ExplicitCacheServerPolicy extends SapphireServerPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private ExplicitCacheGroupPolicy group;

		@Override
		public void onCreate(SapphireGroupPolicy group) {
			// TODO Auto-generated method stub
			this.group = (ExplicitCacheGroupPolicy) group;
		}
		
		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}

		public AppObject getObject() throws Exception {
				return sapphire_getAppObject();
		}

		public void syncObject(Serializable object) {
			appObject.setObject(object);
		}

		@Override
		public void onMembershipChange() {
			// TODO Auto-generated method stub
			
		}
	}

	public static class ExplicitCacheGroupPolicy extends SapphireGroupPolicy {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		ExplicitCacheServerPolicy server;
		
		@Override
		public void addServer(SapphireServerPolicy server) {
			this.server = (ExplicitCacheServerPolicy) server;
		}

		@Override
		public void onFailure(SapphireServerPolicy server) {
			
		}

		@Override
		public SapphireServerPolicy onRefRequest() {
			return server;
		}

		@Override
		public ArrayList<SapphireServerPolicy> getServers() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void onCreate(SapphireServerPolicy server) {
			// TODO Auto-generated method stub
			addServer(server);
		}
	}
}

