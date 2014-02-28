package sapphire.policy;

import java.io.Serializable;
import java.util.ArrayList;

import sapphire.policy.SapphirePolicy.SapphireGroupPolicy;
import sapphire.policy.SapphirePolicy.SapphireServerPolicy;

public interface SapphireDurabilityPolicyUpcalls extends Serializable {
	
	public interface SapphireDurabilityServerUpcalls extends Serializable {
		public void onCreate(SapphireGroupPolicy group);
		public SapphireGroupPolicy getGroup();
		public Object onRPC(String method, ArrayList<Object> params) throws Exception;
		public String getStorageKey();
	}
	
	public interface SapphireDurabilityGroupUpcalls extends Serializable {
		public void onCreate(SapphireServerPolicy server);
		public void addServer(SapphireServerPolicy server);
		public ArrayList<SapphireServerPolicy> getServers();
		public void onFailure(SapphireServerPolicy server);
		public SapphireServerPolicy onRefRequest();
	}
}
