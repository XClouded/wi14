package sapphire.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.logging.Logger;

import sapphire.stats.Stopwatch;
import sapphire.compiler.GlobalStubConstants;
import sapphire.kernel.common.GlobalKernelReferences;

/** 
 * An object handler contains the actual object and pointers to its methods. It basically invokes the method, identified
 * by a String, on the contained object.
 * @author iyzhang
 *
 */

public class ObjectHandlerNewInvoke implements Serializable {
	static private Logger logger = Logger.getLogger(ObjectHandler.class.getName());
	MethodDispatcher methodDispatcher;

	/**
	 * At creation time, we create the actual object, which happens to be the superclass of the stub.
	 * We also inspect the methods of the object to set up a table we can use to look up the method on RPC.
	 * 
	 * @param stub
	 */	
	public ObjectHandlerNewInvoke(Object obj) {
		// create the dispatcher
		String dispatcherClassName = getDispatcherName(obj);
		try {
			Class<?> dispatcherClass = Class.forName(dispatcherClassName);
			Constructor<?> cons = dispatcherClass.getConstructor(Object.class);
			methodDispatcher = (MethodDispatcher) cons.newInstance(obj);
		} catch (Exception e) {
			logger.severe("Could not instantiate  method dispatcher: " + e.getMessage());
		    e.printStackTrace();
		}
	}

	public String getDispatcherName(Object obj) {
		return obj.getClass() + GlobalStubConstants.STUB_SUFFIX;
	}

	/**
	 * Invoke method on the object using the params
	 * 
	 * @param method
	 * @param params
	 * @return the return value from the method
	 */
	public Object invoke(String method, ArrayList<Object> params) throws Exception {
		//Stopwatch timer = new Stopwatch();
		Object ret = methodDispatcher.invoke(method, params);
		//GlobalKernelReferences.stats.log("InvokeTime", timer);
		return ret;
	}

	public Serializable getObject() {
		return (Serializable) methodDispatcher.getActualObject();
	}

	public void setObject(Serializable object) {
		methodDispatcher.setActualObject(object);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(methodDispatcher.getActualObject());
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		Object obj = in.readObject();
		methodDispatcher.setActualObject(obj);
	}
}
