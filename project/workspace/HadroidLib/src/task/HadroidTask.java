package task;

import java.util.List;
import java.util.UUID;

import uw.edu.hadroid.workflow.HadroidFunction;


public class HadroidTask {
    
    private List data;
    private HadroidFunction function;
    private UUID uuid;

    public HadroidTask(List data, HadroidFunction function, UUID uuid) {
        this.data = data;
        this.function = function;
        this.uuid = uuid;
    }


    public List getData() {
        return data;
    }
    public void setData(List data) {
        this.data = data;
    }
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public HadroidFunction getFunction() {
        return function;
    }
    public void setFunction(HadroidFunction function) {
        this.function = function;
    }
 
    
    
}
