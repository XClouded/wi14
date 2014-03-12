package server;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import message.ResultMessage;
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
    
    public void addHadroidJob(String jobClassName){
        Class cls = loadClass(jobClassName);
        HadroidMapReduceJob job;
        try {
            job = (HadroidMapReduceJob) cls.newInstance();
            addHadroidJob(job);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    
    private Class loadClass(String className){
        File file1 = new File("/Users/isphrazy/Documents/study/CSE/550/hw/wi14/project/workspace/HadroidSampleMapReduce/bin/");
        try {
            URL[] urls = {file1.toURI().toURL()};

            URLClassLoader cl = new URLClassLoader(urls);
            return cl.loadClass(className);
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } 
        return null;
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
            if(currentDecomposer.isJobCompleted()) {
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
    
    public void taskIsDone(ResultMessage msg){
        HadroidJobDecomposer d = taskToDecomposer.get(msg.getTaskID());
        d.taskIsDone(msg);
        if(d.isJobCompleted()) taskToDecomposer.remove(d);
    }
}
