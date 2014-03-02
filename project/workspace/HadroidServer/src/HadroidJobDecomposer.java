import task.HadroidTask;
import uw.edu.hadroid.workflow.HadroidJob;

/**
 * 
 *
 */
public class HadroidJobDecomposer {

    private final static int DATA_CHUNK_SIZE = 256 * 1024; //256K
    
    private HadroidJob job;
    
    public HadroidJobDecomposer(HadroidJob job){
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
