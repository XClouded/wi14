/*
 * Stub for class sapphire.appexamples.jeffsdocs.app.Doc
 * Generated by Sapphire Compiler (sc).
 */
package sapphire.appexamples.jeffsdocs.app.stubs;


public final class Doc_Stub extends sapphire.appexamples.jeffsdocs.app.Doc implements sapphire.common.AppObjectStub {

    sapphire.policy.SapphirePolicy.SapphireClientPolicy $__client = null;
    boolean $__directInvocation = false;

    public Doc_Stub (sapphire.appexamples.jeffsdocs.app.User $param_User_1, java.lang.String $param_String_2) {
        super($param_User_1, $param_String_2);
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



    // Implementation of type()
    public java.lang.String type() {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                $__result = super.type();
            else {
                Object[] $__params = null;
                String $__method = "public java.lang.String sapphire.appexamples.jeffsdocs.app.Doc.type()";
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((java.lang.String) $__result);
    }

    // Implementation of toString()
    public java.lang.String toString() {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                $__result = super.toString();
            else {
                Object[] $__params = null;
                String $__method = "public java.lang.String sapphire.appexamples.jeffsdocs.app.Doc.toString()";
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((java.lang.String) $__result);
    }

    // Implementation of setPermissions(User, Data.Permissions)
    public void setPermissions(sapphire.appexamples.jeffsdocs.app.User $param_User_1, sapphire.appexamples.jeffsdocs.app.Data.Permissions $param_Data$Permissions_2) {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                super.setPermissions( $param_User_1,  $param_Data$Permissions_2);
            else {
                Object[] $__params = new Object[2];
                String $__method = "public void sapphire.appexamples.jeffsdocs.app.Doc.setPermissions(sapphire.appexamples.jeffsdocs.app.User,sapphire.appexamples.jeffsdocs.app.Data$Permissions)";
                $__params[0] = $param_User_1;
                $__params[1] = $param_Data$Permissions_2;
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of read(User)
    public java.lang.String read(sapphire.appexamples.jeffsdocs.app.User $param_User_1) {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                $__result = super.read( $param_User_1);
            else {
                Object[] $__params = new Object[1];
                String $__method = "public java.lang.String sapphire.appexamples.jeffsdocs.app.Doc.read(sapphire.appexamples.jeffsdocs.app.User)";
                $__params[0] = $param_User_1;
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((java.lang.String) $__result);
    }

    // Implementation of move(User, int)
    public void move(sapphire.appexamples.jeffsdocs.app.User $param_User_1, int $param_int_2) {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                super.move( $param_User_1,  $param_int_2);
            else {
                Object[] $__params = new Object[2];
                String $__method = "public void sapphire.appexamples.jeffsdocs.app.Doc.move(sapphire.appexamples.jeffsdocs.app.User,int)";
                $__params[0] = $param_User_1;
                $__params[1] = $param_int_2;
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of insert(User, String)
    public void insert(sapphire.appexamples.jeffsdocs.app.User $param_User_1, java.lang.String $param_String_2) {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                super.insert( $param_User_1,  $param_String_2);
            else {
                Object[] $__params = new Object[2];
                String $__method = "public void sapphire.appexamples.jeffsdocs.app.Doc.insert(sapphire.appexamples.jeffsdocs.app.User,java.lang.String)";
                $__params[0] = $param_User_1;
                $__params[1] = $param_String_2;
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of initialize(Data)
    public void initialize(sapphire.appexamples.jeffsdocs.app.Data $param_Data_1) {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                super.initialize( $param_Data_1);
            else {
                Object[] $__params = new Object[1];
                String $__method = "public void sapphire.appexamples.jeffsdocs.app.Doc.initialize(sapphire.appexamples.jeffsdocs.app.Data)";
                $__params[0] = $param_Data_1;
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of highlight(User, int, int)
    public void highlight(sapphire.appexamples.jeffsdocs.app.User $param_User_1, int $param_int_2, int $param_int_3) {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                super.highlight( $param_User_1,  $param_int_2,  $param_int_3);
            else {
                Object[] $__params = new Object[3];
                String $__method = "public void sapphire.appexamples.jeffsdocs.app.Doc.highlight(sapphire.appexamples.jeffsdocs.app.User,int,int)";
                $__params[0] = $param_User_1;
                $__params[1] = $param_int_2;
                $__params[2] = $param_int_3;
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Implementation of hasAccess(User, Data.Permissions)
    public boolean hasAccess(sapphire.appexamples.jeffsdocs.app.User $param_User_1, sapphire.appexamples.jeffsdocs.app.Data.Permissions $param_Data$Permissions_2) {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                $__result = super.hasAccess( $param_User_1,  $param_Data$Permissions_2);
            else {
                Object[] $__params = new Object[2];
                String $__method = "public boolean sapphire.appexamples.jeffsdocs.app.Doc.hasAccess(sapphire.appexamples.jeffsdocs.app.User,sapphire.appexamples.jeffsdocs.app.Data$Permissions)";
                $__params[0] = $param_User_1;
                $__params[1] = $param_Data$Permissions_2;
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((java.lang.Boolean) $__result).booleanValue();
    }

    // Implementation of getUsers(User)
    public java.util.Map getUsers(sapphire.appexamples.jeffsdocs.app.User $param_User_1) {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                $__result = super.getUsers( $param_User_1);
            else {
                Object[] $__params = new Object[1];
                String $__method = "public java.util.Map<java.lang.String, sapphire.appexamples.jeffsdocs.app.Data$Permissions> sapphire.appexamples.jeffsdocs.app.Doc.getUsers(sapphire.appexamples.jeffsdocs.app.User)";
                $__params[0] = $param_User_1;
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((java.util.Map) $__result);
    }

    // Implementation of getLevel(User)
    public sapphire.appexamples.jeffsdocs.app.Data.Permissions getLevel(sapphire.appexamples.jeffsdocs.app.User $param_User_1) {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                $__result = super.getLevel( $param_User_1);
            else {
                Object[] $__params = new Object[1];
                String $__method = "public sapphire.appexamples.jeffsdocs.app.Data$Permissions sapphire.appexamples.jeffsdocs.app.Doc.getLevel(sapphire.appexamples.jeffsdocs.app.User)";
                $__params[0] = $param_User_1;
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((sapphire.appexamples.jeffsdocs.app.Data.Permissions) $__result);
    }

    // Implementation of getCursors(User)
    public java.util.Map getCursors(sapphire.appexamples.jeffsdocs.app.User $param_User_1) {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                $__result = super.getCursors( $param_User_1);
            else {
                Object[] $__params = new Object[1];
                String $__method = "public java.util.Map<java.lang.String, sapphire.appexamples.jeffsdocs.app.Doc$Position> sapphire.appexamples.jeffsdocs.app.Doc.getCursors(sapphire.appexamples.jeffsdocs.app.User)";
                $__params[0] = $param_User_1;
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((java.util.Map) $__result);
    }

    // Implementation of canShareData(User, UserManager, User, Data.Permissions)
    public boolean canShareData(sapphire.appexamples.jeffsdocs.app.User $param_User_1, sapphire.appexamples.jeffsdocs.app.UserManager $param_UserManager_2, sapphire.appexamples.jeffsdocs.app.User $param_User_3, sapphire.appexamples.jeffsdocs.app.Data.Permissions $param_Data$Permissions_4) {
        java.lang.Object $__result = null;
        try {
            if ($__directInvocation)
                $__result = super.canShareData( $param_User_1,  $param_UserManager_2,  $param_User_3,  $param_Data$Permissions_4);
            else {
                Object[] $__params = new Object[4];
                String $__method = "public boolean sapphire.appexamples.jeffsdocs.app.Doc.canShareData(sapphire.appexamples.jeffsdocs.app.User,sapphire.appexamples.jeffsdocs.app.UserManager,sapphire.appexamples.jeffsdocs.app.User,sapphire.appexamples.jeffsdocs.app.Data$Permissions)";
                $__params[0] = $param_User_1;
                $__params[1] = $param_UserManager_2;
                $__params[2] = $param_User_3;
                $__params[3] = $param_Data$Permissions_4;
                $__result = $__client.onRPC($__method, $__params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((java.lang.Boolean) $__result).booleanValue();
    }
}
