import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import task.HadroidTask;
import uw.edu.hadroid.workflow.HadroidMapReduceJob;


public class HadroidJobsManager {
    
    private Queue<HadroidJobDecomposer> jobs;
    private Map<UUID, HadroidJobDecomposer> taskToDecomposer;
    
    public HadroidJobsManager(){
        jobs = new LinkedList<HadroidJobDecomposer>();
        taskToDecomposer = new HashMap<UUID, HadroidJobDecomposer>();
    }
    
    public void addHadroidJob(HadroidMapReduceJob job){
        jobs.add(new HadroidJobDecomposer(job));
    }
    
    /**
     * The client finished the task, update the job status
     */
    public void taskResultReceived(HadroidTask task){
        HadroidJobDecomposer hjd = taskToDecomposer.get(task.getUuid());
    }
    
    /**
     * 
     * @return the next task
     */
    public HadroidTask getNextTask(){
        Iterator<HadroidJobDecomposer> it = jobs.iterator();
        while(it.hasNext()){
            HadroidJobDecomposer currentDecomposer = it.next();
            if(currentDecomposer.isJobDone()) {
                it.remove();
                continue;
            }
            HadroidTask task = currentDecomposer.getNextTask();
            if(task != null){
                taskToDecomposer.put(task.getUuid(), currentDecomposer);
                return task;
            }
        }
        return null;
    }
}
