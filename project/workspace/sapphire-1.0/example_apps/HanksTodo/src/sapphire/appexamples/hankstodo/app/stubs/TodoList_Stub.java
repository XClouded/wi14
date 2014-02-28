/*
 * Stub for class sapphire.appexamples.hankstodo.app.TodoList
 * Generated by Sapphire Compiler (sc).
 */
package sapphire.appexamples.hankstodo.app.stubs;


public final class TodoList_Stub extends sapphire.appexamples.hankstodo.app.TodoList implements sapphire.common.AppObjectStub {

    sapphire.policy.SapphirePolicy.SapphireClientPolicy $__client = null;
    boolean $__directInvocation = false;

    public TodoList_Stub (java.lang.String $param_String_1) {
        super($param_String_1);
    }


    public void $__initialize(sapphire.policy.SapphirePolicy.SapphireClientPolicy client) {
        $__client = client;
    }

    public void $__initialize(boolean directInvocation) {
        $__directInvocation = directInvocation;
    }

    public Object $__clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public sapphire.policy.SapphirePolicy.SapphireClientPolicy $__getReference() {
        return $__client;
    }



    // Implementation of getHighPriority()
    public java.util.ArrayList getHighPriority() {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                $__result = super.getHighPriority();
            else {
                Object[] $__params = null;
                String $__method = "public java.util.ArrayList<java.lang.Object> sapphire.appexamples.hankstodo.app.TodoList.getHighPriority()";
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((java.util.ArrayList) $__result);
    }

    // Implementation of completeToDo(String)
    public void completeToDo(java.lang.String $param_String_1) {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                super.completeToDo( $param_String_1);
            else {
                Object[] $__params = new Object[1];
                String $__method = "public void sapphire.appexamples.hankstodo.app.TodoList.completeToDo(java.lang.String)";
                $__params[0] = $param_String_1;
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of addToDo(String)
    public java.lang.String addToDo(java.lang.String $param_String_1) {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                $__result = super.addToDo( $param_String_1);
            else {
                Object[] $__params = new Object[1];
                String $__method = "public java.lang.String sapphire.appexamples.hankstodo.app.TodoList.addToDo(java.lang.String)";
                $__params[0] = $param_String_1;
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((java.lang.String) $__result);
    }
}