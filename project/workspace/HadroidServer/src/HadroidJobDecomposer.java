import task.HadroidTask;
import uw.edu.hadroid.workflow.HadroidMapReduceJob;

/**
 * 
 *
 */
public class HadroidJobDecomposer {

    private final static int DATA_CHUNK_SIZE = 256 * 1024; //256K
    
    private HadroidMapReduceJob job;
    
    public HadroidJobDecomposer(HadroidMapReduceJob job){
        this.job = job;
    }
    
    /**
     * 
     * @return the next available task. If no task is available, returns
     * null
     * 
     */
    public HadroidTask getNextTask(){
        
        return null;
    }
}
