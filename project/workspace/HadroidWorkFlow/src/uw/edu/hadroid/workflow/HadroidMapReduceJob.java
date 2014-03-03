package uw.edu.hadroid.workflow;
import java.io.File;
import java.util.List;

public abstract class HadroidMapReduceJob {

    protected File inputData;
    protected MapFunction map;
    protected ReduceFunction reduce;
    

    abstract public String getInputFilePath();
    abstract public String getOutputFilePath();
    
    public File getInputData() {
        return inputData;
    }

    public void setInputData(File inputData) {
        this.inputData = inputData;
    }

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
