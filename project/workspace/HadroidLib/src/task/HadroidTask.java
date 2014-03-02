package task;

import uw.edu.hadroid.workflow.HadroidFunction;


public class HadroidTask {
    
    private Object data;
    private HadroidFunction function;

    public HadroidTask(Object data, HadroidFunction function) {
        this.data = data;
        this.function = function;
    }

    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
    public HadroidFunction getFunction() {
        return function;
    }
    public void setFunction(HadroidFunction function) {
        this.function = function;
    }
 
    
    
}
