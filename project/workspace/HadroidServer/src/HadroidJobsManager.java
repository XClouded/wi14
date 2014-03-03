import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import task.HadroidTask;
import uw.edu.hadroid.workflow.HadroidMapReduceJob;


public class HadroidJobsManager {
    
    private Queue<HadroidJobDecomposer> jobs;
    
    public HadroidJobsManager(){
        jobs = new LinkedList<HadroidJobDecomposer>();
    }
    
    public void addHadroidJob(HadroidMapReduceJob job){
        jobs.add(new HadroidJobDecomposer(job));
    }
    
    /**
     * The client finished the task, update the job status
     */
    public void taskResultReceived(HadroidTask task){
        
    }
    
    /**
     * 
     * @return the next task
     */
    public HadroidTask getNextTask(){
        Iterator<HadroidJobDecomposer> it = jobs.iterator();
        while(it.hasNext()){
            HadroidTask task = it.next().getNextTask();
            if(task != null) return task;
        }
        return null;
    }
}
