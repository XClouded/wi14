/*
 * Stub for class sapphire.policy.pubsub.PubSubPolicy.PubSubServerPolicy
 * Generated by Sapphire Compiler (sc).
 */
package sapphire.policy.stubs;


public final class PubSubPolicy$PubSubServerPolicy_Stub extends sapphire.policy.pubsub.PubSubPolicy.PubSubServerPolicy implements sapphire.kernel.common.KernelObjectStub {

    private static final long serialVersionUID = 2L;
    sapphire.kernel.common.KernelOID $__oid = null;
    java.net.InetSocketAddress $__hostname = null;

    public PubSubPolicy$PubSubServerPolicy_Stub(sapphire.kernel.common.KernelOID oid) {
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
        PubSubPolicy$PubSubServerPolicy_Stub other = (PubSubPolicy$PubSubServerPolicy_Stub) obj;
        if (! other.$__oid.equals($__oid))
            return false;
        return true;
    }
    @Override
    public int hashCode() { 
        return $__oid.getID();
    }


    // Implementation of onRPC(String, Object[])
    public java.lang.Object onRPC(java.lang.String $param_String_1, java.lang.Object[] $param_arrayOf_Object_2)
            throws java.lang.Exception {
        String $__method = "public java.lang.Object sapphire.policy.DefaultSapphirePolicyUpcallImpl$DefaultSapphireServerPolicyUpcallImpl.onRPC(java.lang.String,java.lang.Object[]) throws java.lang.Exception";
        Object[] $__params = new Object[2];
        $__params[0] = $param_String_1;
        $__params[1] = $param_arrayOf_Object_2;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return $__result;
    }

    // Implementation of onMembershipChange()
    public void onMembershipChange() {
        String $__method = "public void sapphire.policy.pubsub.PubSubPolicy$PubSubServerPolicy.onMembershipChange()";
        Object[] $__params = null;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of onHighLatency()
    public void onHighLatency() {
        String $__method = "public void sapphire.policy.DefaultSapphirePolicyUpcallImpl$DefaultSapphireServerPolicyUpcallImpl.onHighLatency()";
        Object[] $__params = null;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of onCreate(SapphirePolicy.SapphireGroupPolicy)
    public void onCreate(sapphire.policy.SapphirePolicy.SapphireGroupPolicy $param_SapphirePolicy$SapphireGroupPolicy_1) {
        String $__method = "public void sapphire.policy.pubsub.PubSubPolicy$PubSubServerPolicy.onCreate(sapphire.policy.SapphirePolicy$SapphireGroupPolicy)";
        Object[] $__params = new Object[1];
        $__params[0] = $param_SapphirePolicy$SapphireGroupPolicy_1;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of getGroup()
    public sapphire.policy.SapphirePolicy.SapphireGroupPolicy getGroup() {
        String $__method = "public sapphire.policy.SapphirePolicy$SapphireGroupPolicy sapphire.policy.pubsub.PubSubPolicy$PubSubServerPolicy.getGroup()";
        Object[] $__params = null;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((sapphire.policy.SapphirePolicy.SapphireGroupPolicy) $__result);
    }
}
