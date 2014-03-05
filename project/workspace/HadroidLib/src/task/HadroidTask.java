package task;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import uw.edu.hadroid.workflow.HadroidFunction;


public class HadroidTask implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = -1192939452226482886L;
    
    private List data;
    private byte[] dexFile; //dex file
    private String className; //name of the class
    private UUID uuid;

    public HadroidTask(List data, byte[] dexFile, String className, UUID uuid) {
        this.data = data;
        this.dexFile = dexFile;
        this.className = className;
        this.uuid = uuid;
    }

    public List getData() {
        return data;
    }
    public void setData(List data) {
        this.data = data;
    }
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }


    public byte[] getDexFile() {
        return dexFile;
    }


    public void setDexFile(byte[] dexFile) {
        this.dexFile = dexFile;
    }


    public String getClassName() {
        return className;
    }


    public void setClassName(String className) {
        this.className = className;
    }

}
