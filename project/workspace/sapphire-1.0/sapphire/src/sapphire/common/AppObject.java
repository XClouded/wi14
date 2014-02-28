package sapphire.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class AppObject extends ObjectHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected Class<?> getClass(Object obj) {
		return obj.getClass().getSuperclass();
	}

	public AppObject(Object obj) {
		super(obj);
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(super.getObject());
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		Object obj = in.readObject();
		super.fillMethodTable(obj);
		super.setObject((Serializable)obj);
	}
}
