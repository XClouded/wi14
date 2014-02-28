/*
 * Stub for class sapphire.policy.replicate.ConsensusReplication.ConsensusReplicateServerPolicy
 * Generated by Sapphire Compiler (sc).
 */
package sapphire.policy.stubs;


public final class ConsensusReplication$ConsensusReplicateServerPolicy_Stub extends sapphire.policy.replicate.ConsensusReplication.ConsensusReplicateServerPolicy implements sapphire.kernel.common.KernelObjectStub {

    private static final long serialVersionUID = 2L;
    sapphire.kernel.common.KernelOID $__oid = null;
    java.net.InetSocketAddress $__hostname = null;

    public ConsensusReplication$ConsensusReplicateServerPolicy_Stub(sapphire.kernel.common.KernelOID oid) {
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
        ConsensusReplication$ConsensusReplicateServerPolicy_Stub other = (ConsensusReplication$ConsensusReplicateServerPolicy_Stub) obj;
        if (! other.$__oid.equals($__oid))
            return false;
        return true;
    }
    @Override
    public int hashCode() { 
        return $__oid.getID();
    }


    // Implementation of setView(ConsensusReplication.View)
    public void setView(sapphire.policy.replicate.ConsensusReplication.View $param_ConsensusReplication$View_1) {
        String $__method = "public synchronized void sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy.setView(sapphire.policy.replicate.ConsensusReplication$View)";
        Object[] $__params = new Object[1];
        $__params[0] = $param_ConsensusReplication$View_1;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of setObject(int, Serializable, int)
    public void setObject(int $param_int_1, java.io.Serializable $param_Serializable_2, int $param_int_3) {
        String $__method = "public void sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy.setObject(int,java.io.Serializable,int)";
        Object[] $__params = new Object[3];
        $__params[0] = $param_int_1;
        $__params[1] = $param_Serializable_2;
        $__params[2] = $param_int_3;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of onRPC(String, Object[])
    public java.lang.Object onRPC(java.lang.String $param_String_1, java.lang.Object[] $param_arrayOf_Object_2)
            throws java.lang.Exception {
        String $__method = "public java.lang.Object sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy.onRPC(java.lang.String,java.lang.Object[]) throws java.lang.Exception";
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
        String $__method = "public void sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy.onMembershipChange()";
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
        String $__method = "public void sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy.onCreate(sapphire.policy.SapphirePolicy$SapphireGroupPolicy)";
        Object[] $__params = new Object[1];
        $__params[0] = $param_SapphirePolicy$SapphireGroupPolicy_1;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of leaderOnRPC(int, String, Object[])
    public java.lang.Object leaderOnRPC(int $param_int_1, java.lang.String $param_String_2, java.lang.Object[] $param_arrayOf_Object_3)
            throws java.lang.Exception {
        String $__method = "public synchronized java.lang.Object sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy.leaderOnRPC(int,java.lang.String,java.lang.Object[]) throws java.lang.Exception";
        Object[] $__params = new Object[3];
        $__params[0] = $param_int_1;
        $__params[1] = $param_String_2;
        $__params[2] = $param_arrayOf_Object_3;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return $__result;
    }

    // Implementation of getRPCNum()
    public int getRPCNum() {
        String $__method = "public int sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy.getRPCNum()";
        Object[] $__params = null;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((java.lang.Integer) $__result).intValue();
    }

    // Implementation of getPermission(int)
    public boolean getPermission(int $param_int_1) {
        String $__method = "public boolean sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy.getPermission(int)";
        Object[] $__params = new Object[1];
        $__params[0] = $param_int_1;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((java.lang.Boolean) $__result).booleanValue();
    }

    // Implementation of getObject()
    public java.io.Serializable getObject() {
        String $__method = "public java.io.Serializable sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy.getObject()";
        Object[] $__params = null;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((java.io.Serializable) $__result);
    }

    // Implementation of getGroup()
    public sapphire.policy.SapphirePolicy.SapphireGroupPolicy getGroup() {
        String $__method = "public sapphire.policy.SapphirePolicy$SapphireGroupPolicy sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy.getGroup()";
        Object[] $__params = null;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((sapphire.policy.SapphirePolicy.SapphireGroupPolicy) $__result);
    }

    // Implementation of beginLeading()
    public void beginLeading()
            throws sapphire.common.SapphireObjectNotAvailableException {
        String $__method = "public void sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy.beginLeading() throws sapphire.common.SapphireObjectNotAvailableException";
        Object[] $__params = null;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of ConsensusReplicatePin(String)
    public void ConsensusReplicatePin(java.lang.String $param_String_1)
            throws java.rmi.RemoteException {
        String $__method = "public void sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy.ConsensusReplicatePin(java.lang.String) throws java.rmi.RemoteException";
        Object[] $__params = new Object[1];
        $__params[0] = $param_String_1;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of ConsensusReplicate()
    public sapphire.policy.replicate.ConsensusReplication.ConsensusReplicateServerPolicy ConsensusReplicate() {
        String $__method = "public sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy sapphire.policy.replicate.ConsensusReplication$ConsensusReplicateServerPolicy.ConsensusReplicate()";
        Object[] $__params = null;
        java.lang.Object $__result = null;
        try {
            $__result = $__makeKernelRPC($__method, $__params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((sapphire.policy.replicate.ConsensusReplication.ConsensusReplicateServerPolicy) $__result);
    }
}
