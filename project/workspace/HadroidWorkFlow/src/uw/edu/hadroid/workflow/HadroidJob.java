package uw.edu.hadroid.workflow;
import java.util.LinkedList;
import java.util.List;

/**
 * Each HadroidJob contains a data and a list of task
 * that should be run. The first task's input is the original data.
 * The return value of each task will be the input of next task. The 
 * return value of the last task will be returned to customer. 
 */
public class HadroidJob {

    public Object data;
    public List<HadroidFunction> functions;
    
    public HadroidJob(Object data) {
        this.data = data;
        functions = new LinkedList<HadroidFunction>();
    }

    public void addHadroidTask(HadroidFunction function){
        functions.add(function);
    }
}
