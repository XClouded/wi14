/*
 * Stub for class sapphire.policy.replicate.MasterSlavePolicy.MasterSlaveGroupPolicy
 * Generated by Sapphire Compiler (sc).
 */
package sapphire.policy.stubs;


public final class MasterSlavePolicy$MasterSlaveGroupPolicy_Stub extends sapphire.policy.replicate.MasterSlavePolicy.MasterSlaveGroupPolicy implements sapphire.kernel.common.KernelObjectStub {

    private static final long serialVersionUID = 2L;
    sapphire.kernel.common.KernelOID $__oid = null;
    java.net.InetSocketAddress $__hostname = null;

    public MasterSlavePolicy$MasterSlaveGroupPolicy_Stub(sapphire.kernel.common.KernelOID oid) {
        this.$__oid = oid;
    }

    public sapphire.kernel.common.KernelOID $__getKernelOID() {
        return this.$__oid;
    }

    public java.net.InetSocketAddress $__getHostname() {
        return this.$__hostname;
    }

    public void $__updateHostname(java.net.InetSocketAddress hostname) {
        this.$__hostname = hostname;
    }

    public Object $__makeKernelRPC(java.lang.String method, Object[] params) throws java.rmi.RemoteException, java.lang.Exception {
        try {
            return sapphire.kernel.common.GlobalKernelReferences.nodeServer.getKernelClient().makeKernelRPC(this, $__oid, method, params);
        } catch (sapphire.kernel.common.KernelObjectNotFoundException e) {
            throw new java.rmi.RemoteException();
        }
    }

    @Override
    public boolean equals(Object obj) { 
        MasterSlavePolicy$MasterSlaveGroupPolicy_Stub other = (MasterSlavePolicy$MasterSlaveGroupPolicy_Stub) obj;
        if (! other.$__oid.equals($__oid))
            return false;
        return true;
    }
    @Override
    public int hashCode() { 
        return $__oid.getID();
    }


    // Implementation of onRefRequest()
    public sapphire.policy.SapphirePolicy.SapphireServerPolicy onRefRequest() {
        String $__method = "public sapphire.policy.SapphirePolicy$SapphireServerPolicy sapphire.policy.DefaultSapphirePolicyUpcallImpl$DefaultSapphireGroupPolicyUpcallImpl.onRefRequest()";
        Object[] $__params = null;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((sapphire.policy.SapphirePolicy.SapphireServerPolicy) $__result);
    }

    // Implementation of onFailure(SapphirePolicy.SapphireServerPolicy)
    public void onFailure(sapphire.policy.SapphirePolicy.SapphireServerPolicy $param_SapphirePolicy$SapphireServerPolicy_1) {
        String $__method = "public void sapphire.policy.replicate.MasterSlavePolicy$MasterSlaveGroupPolicy.onFailure(sapphire.policy.SapphirePolicy$SapphireServerPolicy)";
        Object[] $__params = new Object[1];
        $__params[0] = $param_SapphirePolicy$SapphireServerPolicy_1;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of onCreate(SapphirePolicy.SapphireServerPolicy)
    public void onCreate(sapphire.policy.SapphirePolicy.SapphireServerPolicy $param_SapphirePolicy$SapphireServerPolicy_1) {
        String $__method = "public void sapphire.policy.replicate.MasterSlavePolicy$MasterSlaveGroupPolicy.onCreate(sapphire.policy.SapphirePolicy$SapphireServerPolicy)";
        Object[] $__params = new Object[1];
        $__params[0] = $param_SapphirePolicy$SapphireServerPolicy_1;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of getServers()
    public java.util.ArrayList getServers() {
        String $__method = "public java.util.ArrayList<sapphire.policy.SapphirePolicy$SapphireServerPolicy> sapphire.policy.replicate.MasterSlavePolicy$MasterSlaveGroupPolicy.getServers()";
        Object[] $__params = null;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((java.util.ArrayList) $__result);
    }

    // Implementation of addServer(SapphirePolicy.SapphireServerPolicy)
    public void addServer(sapphire.policy.SapphirePolicy.SapphireServerPolicy $param_SapphirePolicy$SapphireServerPolicy_1) {
        String $__method = "public void sapphire.policy.replicate.MasterSlavePolicy$MasterSlaveGroupPolicy.addServer(sapphire.policy.SapphirePolicy$SapphireServerPolicy)";
        Object[] $__params = new Object[1];
        $__params[0] = $param_SapphirePolicy$SapphireServerPolicy_1;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of $__initialize(String, ArrayList)
    public void $__initialize(java.lang.String $param_String_1, java.util.ArrayList $param_ArrayList_2) {
        String $__method = "public void sapphire.policy.DefaultSapphirePolicyUpcallImpl$DefaultSapphireGroupPolicyUpcallImpl.$__initialize(java.lang.String,java.util.ArrayList<java.lang.Object>)";
        Object[] $__params = new Object[2];
        $__params[0] = $param_String_1;
        $__params[1] = $param_ArrayList_2;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
