package sapphire.common;

import java.io.Serializable;
import java.util.ArrayList;

public interface MethodDispatcher extends Serializable {
	public Object invoke(String method, ArrayList<Object> params) throws Exception;
	public Object getActualObject();
	public void setActualObject(Object obj); 
}