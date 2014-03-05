package message;

import java.util.List;
import java.util.UUID;

public class ResultMessage extends HadroidMessage{

    /**
     * 
     */
    private static final long serialVersionUID = 1020653630896918062L;
    
    private UUID taskID;
    private List result;
    
    public ResultMessage(UUID taskID, List result) {
        super();
        this.taskID = taskID;
        this.result = result;
    }
    
    public UUID getTaskID() {
        return taskID;
    }
    public void setTaskID(UUID taskID) {
        this.taskID = taskID;
    }
    public List getResult() {
        return result;
    }
    public void setResult(List result) {
        this.result = result;
    }
    
}
