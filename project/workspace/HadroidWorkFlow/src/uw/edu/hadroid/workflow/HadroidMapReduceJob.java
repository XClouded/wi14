package uw.edu.hadroid.workflow;
import java.io.File;
import java.io.Serializable;
import java.util.List;

public abstract class HadroidMapReduceJob implements Serializable{

    protected HadroidFunction map;
    protected HadroidFunction reduce;
    

    abstract public String getInputFilePath();
    abstract public String getOutputFilePath();
    
    public HadroidFunction getMap() {
        return map;
    }

    public void setMap(HadroidFunction map) {
        this.map = map;
    }

    public HadroidFunction getReduce() {
        return reduce;
    }

    public void setReduce(HadroidFunction reduce) {
        this.reduce = reduce;
    }
    
    public String getName(){
        return this.getClass().getName();
    }

}
