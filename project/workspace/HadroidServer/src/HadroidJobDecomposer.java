import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import task.HadroidTask;
import uw.edu.hadroid.workflow.HadroidMapReduceJob;

/**
 * 
 *
 */
public class HadroidJobDecomposer {

    private final static int DATA_CHUNK_SIZE = 256 * 1024; //256K
    
    private HadroidMapReduceJob job;
    private RandomAccessFile raf;
    private File inputFile;
    //input chunk that is required to apply map function
    private Queue<FileChunk> chunkUndone; 
    //input chunk that is assign to a worker
    private Set<FileChunk> chunkInProgress; 
    //input chunk that is finished
    private Set<FileChunk> chunkDone;
    
    public HadroidJobDecomposer(HadroidMapReduceJob job){
        this.job = job;
        inputFile = new File(job.getInputFilePath());
        chunkUndone = new LinkedList<FileChunk>();
        chunkInProgress = new HashSet<FileChunk>();
        chunkDone = new HashSet<FileChunk>();
        setupChunk();
        try {
            raf = new RandomAccessFile(job.getInputFilePath(), "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * mark the chunks start/end location of the input file
     */
    private void setupChunk(){
        Scanner sc = new Scanner(job.getInputFilePath());
        long chunkStart = 0;
        int chunkSize = 0;
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            if(line.trim().isEmpty()) continue;//ignore empty line
            if(chunkSize + line.length()> DATA_CHUNK_SIZE){
                if(chunkSize == 0){
                    //a single line is greater than max chunk size allowed
                    chunkSize = line.length() + 1;
                }
                chunkUndone.add(new FileChunk(chunkStart, chunkSize));
                chunkStart = chunkStart + chunkSize + 1;
                chunkSize = 0;
            }
            chunkSize += line.length() + 1;
        }
        if(chunkSize > 0){ //something remains
            chunkUndone.add(new FileChunk(chunkStart, chunkSize));
        }
        sc.close();
    }
    
    
    /**
     * 
     * @return true when this job is finished
     */
    public boolean isJobDone(){
        //TODO
        return false;
    }
    
    public void taskIsDone(HadroidTask task){
        //if map task, write to disk
    }
    
    /**
     * 
     * @return the next available task. If no task is available, returns
     * null
     * 
     */
    public HadroidTask getNextTask(){
        
        HadroidTask task = null;
        System.out.println("next task in decomposer");
        if(!chunkUndone.isEmpty()){//still some map work need to be done
            System.out.println("not empty");
            FileChunk fc = chunkUndone.remove();
            System.out.println("fc: " + fc.startPos);
            try {
                raf.seek(fc.startPos);
                byte[] data = new byte[fc.size];
                raf.read(data);
                
                //convert raw data to line list
                InputStream is = new ByteArrayInputStream(data);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                List<String> inputData = new ArrayList<String>();
                inputData.add(reader.readLine());
                task = new HadroidTask(inputData, job.getMap(), UUID.randomUUID());
                chunkInProgress.add(fc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(chunkInProgress.isEmpty()){//all map work is done
            //TODO return some reduce task if available
        }
        return task;
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
        
        
    }
    
}
