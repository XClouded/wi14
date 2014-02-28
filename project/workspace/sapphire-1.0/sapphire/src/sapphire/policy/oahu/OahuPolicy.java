package sapphire.policy.oahu;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sapphire.common.AppObject;
import sapphire.policy.SapphirePolicy;
import sapphire.policy.SapphirePolicy.SapphireClientPolicy;
import sapphire.policy.SapphirePolicy.SapphireServerPolicy;

public class OahuPolicy extends SapphirePolicy {

	public static class OahuClientPolicy extends SapphireClientPolicy {

		private static final long serialVersionUID = 1L;
		public transient OahuServerPolicy server;
		public OahuGroupPolicy group;
		private boolean isHere = false;
		private AppObject localCopy;
		
		private static final double learningFactor = 0.1;
		private Map<String,Double> profilerHere = new HashMap<String,Double>();
		private Map<String,Double> profilerThere = new HashMap<String,Double>();
		private double moveHere = 0.0; //time cost to move the object here
		
		private double batteryValue = 0.3;
		private double networkRTCost = 1.0; //energy per time used when transmitting
		
		private double runningPower = 1.0; //energy per time used when running code
		private double idlePower = 0.4;    //energy per time used when idle
		
		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (OahuGroupPolicy)group;
		}

		@Override
		public void setServer(SapphireServerPolicy server) {
			this.server = (OahuServerPolicy)server;
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
			if(server == null)
				server = (OahuServerPolicy) group.onRefRequest();
			
			if(executeHere(method)) {
				if(!isHere) {
					System.out.println("Fetching copy: " + method);
					long start = System.nanoTime();
					localCopy = server.pullCopy();
					moveHere = moveHere*(1-learningFactor) + (System.nanoTime() - start)*learningFactor;
					isHere = true;
				}
				long start = System.nanoTime();
				Object out = localCopy.invoke(method, params);
				Double prev = profilerHere.get(method);
				if(prev == null)
					prev = 0.0;
				profilerHere.put(method,prev*(1-learningFactor) + (System.nanoTime() - start)*learningFactor);
				System.out.println(prev);
				return out;
			} else {
				long start = System.nanoTime();
				Object out = null;
				if(isHere) {
					isHere = false;
					out =  server.onRPC(localCopy,method,params);
				} else {
					out = server.onRPC(method, params);
				}
				Double prev = profilerThere.get(method);
				if(prev == null)
					prev = 0.0;
				profilerThere.put(method,prev*(1-learningFactor) + (System.nanoTime() - start)*learningFactor);
				return out;
			}
		}

		private boolean executeHere(String method) {
			Double here = profilerHere.get(method);
			if(here == null)
				return true;
			Double there = profilerThere.get(method);
			there = null;
			if(there == null)
				return false;
			return (1+batteryValue*runningPower)*here + (!isHere ? (1+batteryValue*networkRTCost)*moveHere : 0.0) < 
					(1+batteryValue*idlePower)*there + (1+batteryValue*networkRTCost)*moveHere;
		}
	}
	
	public static class OahuServerPolicy extends SapphireServerPolicy {
		private static final long serialVersionUID = 1L;
		public OahuGroupPolicy group;
		
		@Override
		public void onCreate(SapphireGroupPolicy group) {
			this.group = (OahuGroupPolicy)group;
		}

		public Object onRPC(AppObject copy, String method, Object[] params) throws Exception {
			appObject.setObject(copy.getObject());
			return this.onRPC(method, params);
		}

		public AppObject pullCopy() {
			System.out.println("This should print on the server!!!!!!!!!!");
			return appObject;
		}

		@Override
		public SapphireGroupPolicy getGroup() {
			return group;
		}

		@Override
		public void onMembershipChange() {}
		
		public void oahuPin(String region) throws RemoteException {
			sapphire_pin(region);
		}
	}
	
	public static class OahuGroupPolicy extends SapphireGroupPolicy {
		private static final long serialVersionUID = 1L;
		private OahuServerPolicy server;
		
		@Override
		public void onCreate(SapphireServerPolicy server) {
			this.server = (OahuServerPolicy)server;
			try {
				ArrayList<String> regions = this.sapphire_getRegions();
				String serv = null;
				for(String s : regions) {
					System.out.println(s);
					serv = s;
				}
				System.out.println("pinning to " + serv);
				this.server.oahuPin(serv);
			} catch(RemoteException e) {
				System.out.println(e);
			}
		}

		@Override
		public void addServer(SapphireServerPolicy server) {
		}

		@Override
		public ArrayList<SapphireServerPolicy> getServers() {
			return null;
		}
		
		@Override
		public SapphireServerPolicy onRefRequest() {
			return server;
		}

		@Override
		public void onFailure(SapphireServerPolicy server) {}
		
	}
	
}
