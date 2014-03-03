package uw.edu.hadroid.workflow;
import java.io.File;
import java.io.Serializable;
import java.util.List;

public abstract class HadroidMapReduceJob implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 6970608919535025001L;
    protected MapFunction map;
    protected ReduceFunction reduce;
    

    abstract public String getInputFilePath();
    abstract public String getOutputFilePath();
    
    public MapFunction getMap() {
        return map;
    }

    public void setMap(MapFunction map) {
        this.map = map;
    }

    public ReduceFunction getReduce() {
        return reduce;
    }

    public void setReduce(ReduceFunction reduce) {
        this.reduce = reduce;
    }

}
