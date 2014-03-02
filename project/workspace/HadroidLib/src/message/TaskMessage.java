package message;

import task.HadroidTask;

public class TaskMessage extends HadroidMessage {

    /**
     * 
     */
    private static final long serialVersionUID = -2627676160095878108L;

    private HadroidTask task;

    /**
     * @param task the task needs to be done.
     * If task is null, then ther is no available task
     */
    public TaskMessage(HadroidTask task) {
        this.task = task;
    }
    
    public HadroidTask getTask() {
        return task;
    }

    public void setTask(HadroidTask task) {
        this.task = task;
    }
}
