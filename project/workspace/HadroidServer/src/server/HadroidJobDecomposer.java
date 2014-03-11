package server;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
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
    
    private enum Phase{
        INIT, MAP, REDUCE, COMPLETE
    }
    
    private HadroidMapReduceJob job;
    private RandomAccessFile raf;
    private File inputFile;
    //input chunk that is required to apply map function
    private Queue<FileChunk> chunkUndone; 
    //input chunk that is assign to a worker
    private Map<UUID, HadroidTask> mapInProgress;
    //input chunk that is finished
    private Set<HadroidTask> mapDone;
    private Set<String> intermediateKeysUndone;
//    private Set<String> intermediateKeysInProgress;
    private Map<UUID, HadroidTask> reduceTasks;
    private String intermediateDir;
    private Logger logger;
    private Phase phase;
    
    public HadroidJobDecomposer(HadroidMapReduceJob job){
        this.job = job;
        inputFile = new File(job.getInputFilePath());
        chunkUndone = new LinkedList<FileChunk>();
        mapInProgress = new HashMap<UUID, HadroidTask>();
        mapDone = new HashSet<HadroidTask>();
        intermediateKeysUndone = new LinkedHashSet<String>();
//        intermediateKeysInProgress = new HashSet<String>();
        reduceTasks = new HashMap<UUID, HadroidTask>();
        
        logger = Logger.getLogger(this.getClass().getName());
        phase = Phase.INIT;
        try {
            raf = new BufferedRandomAccessFile(job.getInputFilePath(), "r");
            setupChunks();
            phase = Phase.MAP;
            setupFolder();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void setupFolder() throws IOException {
        File tmp = new File("db/" + job.getName() + "/tmp/");
        File output = new File("db/" + job.getName() + "/output/");
        if(!tmp.exists()) tmp.mkdirs();
        if(!output.exists()) output.mkdirs();
    }

    /**
     * mark the chunks start/end location of the input file
     * @throws IOException 
     */
    private void setupChunks() throws IOException{
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
        return phase == Phase.COMPLETE;
    }
    
    public void taskIsDone(ResultMessage msg){
        UUID taskID = msg.getTaskID();
        List result = msg.getResult();
//        if(chunkInProgress.)
        if(phase == Phase.MAP){ //currently in map phase
            //record this update
            HadroidTask task = mapInProgress.get(taskID);
            
            intermediateKeysUndone.addAll(getKeysFromResult(result));
            //partition and write to disk
            try {
                saveIntermediateResult(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mapInProgress.remove(taskID);
            mapDone.add(task);
            if(chunkUndone.isEmpty() && mapInProgress.isEmpty()){
                phase = Phase.REDUCE;
            }
        }else if(phase == Phase.REDUCE){
            try {
                saveFinalResult(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
            reduceTasks.remove(taskID);
            if(reduceTasks.isEmpty()){
                //if all reduce job is done, then the job is completed.
                phase = Phase.COMPLETE;
            }
        }
    }
    
    private List<String> getKeysFromResult(List pairs) {
        List<String> result = new LinkedList<String>();
        for(Object o : pairs){
            if(!(o instanceof Pair)){
//                System.err.println("HadroidJobDecomposer: wrong result type");
                logger.severe("result element has wrong type");
                break;
            }
            Pair p = (Pair)o;
            //TODO generalize this
            result.add((String)p.key);
        }
        return result;
    }

    private void saveIntermediateResult(List result) throws IOException{
        writeKeyValuesToDisk(result, "tmp");
    }
    
    private void saveFinalResult(List result) throws IOException{
        writeKeyValuesToDisk(result, "output");
    }
    
    /**
     * write to each 
     * @param result
     * @throws IOException 
     */
    private void writeKeyValuesToDisk(List result, String subdir) throws IOException {
        Map<Object, PrintWriter> keyToChannel = new HashMap<Object, PrintWriter>();
        for(Object o : result){
            if(!(o instanceof Pair)){
                logger.severe("result element has wrong type");
                return;
            }
            Pair p = (Pair)o;
            logger.severe("key: " + p.key + " value: " + p.value);
            
            if(keyToChannel.containsKey(p.key)){
                PrintWriter writer = keyToChannel.get(p.key);
                writer.append(p.value.toString() + "\n");
            }else{
                String filePath = DATABASE_LOCATION + job.getName() + "/" + subdir + "/" + p.key + ".interm";
                File f = new File(filePath);
                f.createNewFile();
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
                keyToChannel.put(p.key, writer);
            }
        }
        //close all the writers. 
        for(PrintWriter writer : keyToChannel.values()){
            writer.close();
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
//        if(!chunkUndone.isEmpty()){//still some map work need to be done
        try {
            if(phase == Phase.MAP){//still some map work need to be done
                FileChunk fc = chunkUndone.remove();
                raf.seek(fc.startPos);
                byte[] data = new byte[fc.size];
                raf.read(data);
                List<String> inputData = parseInputData(data);
                String dexFilePath = "/Users/isphrazy/Documents/study/CSE/550/hw/wi14/project/workspace/HadroidSampleMapReduce/WordCounterMapDex.jar";
                byte[] dexFile = dexFileToByteArray(dexFilePath);
                task = new HadroidTask(inputData, dexFile, 
                        job.getMap().getClass().getName(), 
                        UUID.randomUUID());
                mapInProgress.put(task.getUuid(), task);
    //        }else if(mapInProgress.isEmpty()){//all map work is done
            }else if(phase == Phase.REDUCE){//all map work is done
                //TODO return some reduce task if available
                String nextKey = intermediateKeysUndone.iterator().next();
//                intermediateKeysInProgress.add(nextKey);
    //            File f = new File(DATABASE_LOCATION + job.getName() + "/tmp/" + nextKey + ".interm");
                String fileName = DATABASE_LOCATION + job.getName() + "/tmp/" + nextKey + ".interm";
                byte[] data = Files.readAllBytes(Paths.get(fileName));
                List<String> intermediatePairs = parseInputData(data);
                String dexFilePath = "/Users/isphrazy/Documents/study/CSE/550/hw/wi14/project/workspace/HadroidSampleMapReduce/WordCounterReduceDex.jar";
                task = new HadroidTask(intermediatePairs, 
                        dexFileToByteArray(dexFilePath), 
                        job.getReduce().getClass().getName(), 
                        UUID.randomUUID());
                reduceTasks.put(task.getUuid(), task);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return task;
    }

    private byte[] dexFileToByteArray(String dexFilePath) throws FileNotFoundException, IOException {
        //read dex file to byte array
//        File file = new File("/Users/isphrazy/Documents/study/CSE/550/hw/wi14/project/workspace/HadroidSampleMapReduce/WordCounterMapDex.jar");
        File dexFile = new File(dexFilePath);
        byte[] fileData = new byte[(int) dexFile.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(dexFile));
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
