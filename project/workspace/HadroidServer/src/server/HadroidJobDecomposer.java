package server;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import message.ResultMessage;
import task.HadroidTask;
import uw.edu.hadroid.workflow.HadroidMapReduceJob;
import uw.edu.hadroid.workflow.Pair;

/**
 * 
 *
 */
public class HadroidJobDecomposer {

    private final static int DATA_CHUNK_SIZE = 256 * 1024; //256K
    private final static String DATABASE_LOCATION = "db/";
    
    private HadroidMapReduceJob job;
    private RandomAccessFile raf;
    private File inputFile;
    //input chunk that is required to apply map function
    private Queue<FileChunk> chunkUndone; 
    //input chunk that is assign to a worker
    private Map<UUID, HadroidTask> mapInProgress;
    //input chunk that is finished
    private Set<HadroidTask> mapDone;
    private Set<HadroidTask> reduceTasks;
    private String intermediateDir;
    private Logger logger;
    
    public HadroidJobDecomposer(HadroidMapReduceJob job){
        this.job = job;
        inputFile = new File(job.getInputFilePath());
        chunkUndone = new LinkedList<FileChunk>();
        mapInProgress = new HashMap<UUID, HadroidTask>();
        mapDone = new HashSet<HadroidTask>();
        reduceTasks = new HashSet<HadroidTask>();
        logger = Logger.getLogger(this.getClass().getName());
        try {
            raf = new BufferedRandomAccessFile(job.getInputFilePath(), "r");
            setupChunk();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * mark the chunks start/end location of the input file
     * @throws IOException 
     */
    private void setupChunk() throws IOException{
//        
        raf.seek(0);
        long chunkStart = 0;
        long chunkEnd = 0;
        long chunkSize = 0;
        String line = null;
        while((line = raf.readLine()) != null){
//            String line = sc.nextLine();
            if(raf.getFilePointer() - chunkStart >= DATA_CHUNK_SIZE){
                
                chunkUndone.add(new FileChunk(chunkStart, (int)chunkSize));
                chunkStart = chunkEnd + 1;
                chunkEnd = raf.getFilePointer() - 1;
                chunkSize = chunkEnd - chunkStart;
            }else{
                chunkEnd = raf.getFilePointer() - 1;
                chunkSize = chunkEnd - chunkStart;
            }
        }
        if(chunkSize > 0){ //something remains
            chunkUndone.add(new FileChunk(chunkStart, (int)chunkSize));
        }
//        reader.close();
    }
    
    
    /**
     * 
     * @return true when this job is finished
     */
    public boolean isJobDone(){
        //TODO
        return false;
    }
    
    public void taskIsDone(ResultMessage msg){
        UUID taskID = msg.getTaskID();
        List result = msg.getResult();
//        if(chunkInProgress.)
        //record this update
        HadroidTask task = mapInProgress.get(taskID);
        mapInProgress.remove(taskID);
        mapDone.add(task);
        
        //partition and write to disk
        writeIntermediate(result);
    }
    
    /**
     * write to each 
     * @param result
     */
    private void writeIntermediate(List result) {
        String jobName = job.getClass().getName();
        for(Object o : result){
            if(!(o instanceof Pair)){
                System.err.println("HadroidJobDecomposer: wrong result type");
                return;
            }
            Pair p = (Pair)o;
            
        }
    }

    /**
     * 
     * @return the next available task. If no task is available, returns
     * null
     * 
     */
    public HadroidTask getNextTask(){
        
        HadroidTask task = null;
        if(!chunkUndone.isEmpty()){//still some map work need to be done
            FileChunk fc = chunkUndone.remove();
            try {
                raf.seek(fc.startPos);
                byte[] data = new byte[fc.size];
                raf.read(data);
                List<String> inputData = parseInputData(data);
                byte[] fileData = dexFileToByteArray();
                task = new HadroidTask(inputData, fileData, 
                        job.getMap().getClass().getName(), 
                        UUID.randomUUID());
                mapInProgress.put(task.getUuid(), task);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(mapInProgress.isEmpty()){//all map work is done
            //TODO return some reduce task if available
            
        }
        return task;
    }

    private byte[] dexFileToByteArray() throws FileNotFoundException, IOException {
        //read dex file to byte array
        File file = new File("/Users/isphrazy/Documents/study/CSE/550/hw/wi14/project/workspace/HadroidSampleMapReduce/WordCounterMapDex.jar");
        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(fileData);
        dis.close();
        return fileData;
    }

    /*
     * convert raw data to line list
     */
    private List<String> parseInputData(byte[] data) throws IOException {
        InputStream is = new ByteArrayInputStream(data);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        List<String> inputData = new ArrayList<String>();
        String line = null;
        while((line = reader.readLine()) != null){
            if(line.trim().isEmpty()) continue;//ignore empty line
            inputData.add(line);
        }
        return inputData;
    }
    
    /**
     * mark the start position and the size of the chunk
     *
     */
    private class FileChunk{
        public long startPos;
        public int size;
        
        public FileChunk(long startPos, int size) {
            this.startPos = startPos;
            this.size = size;
        }
        
        public String toString(){
            return "start: " + startPos + " size: " + size;
        }
    }
    
}
