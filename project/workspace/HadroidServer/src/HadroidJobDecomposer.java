import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

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
    private Queue<FileChunk> chunkUndone;
    private Queue<FileChunk> chunkInProgress;
    private Queue<FileChunk> chunkDone;
    
    public HadroidJobDecomposer(HadroidMapReduceJob job){
        this.job = job;
        inputFile = new File(job.getInputFilePath());
        chunkUndone = new LinkedList<FileChunk>();
        chunkInProgress = new LinkedList<FileChunk>();
        chunkDone = new LinkedList<FileChunk>();
        setupChunk();
        try {
            raf = new RandomAccessFile(job.getInputFilePath(), "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private void setupChunk(){
        Scanner sc = new Scanner(job.getInputFilePath());
        int chunkSize = 0;
        int chunkStart = 0;
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            if(line.trim().isEmpty()) continue;//ignore empty line
            if(chunkSize + line.length()> DATA_CHUNK_SIZE){
                if(chunkSize == 0){
                    //a single line is greater than max chunk size allowed
                    chunkUndone.add(new FileChunk(chunkStart, chunkStart + line.length()));
                }else{
                    
                    chunkUndone.add(new FileChunk(chunkStart, chunkStart + chunkSize));
                    chunkStart = chunkStart + chunkSize + 1;
                    chunkSize = 0;
                }
            }
            chunkSize += line.length();
        }
        if(chunkSize > 0){ //something remains
            chunkUndone.add(new FileChunk(chunkStart, chunkStart + chunkSize));
        }
        sc.close();
    }
    
    public void taskIsDone(HadroidTask task){
        
    }
    
    /**
     * 
     * @return the next available task. If no task is available, returns
     * null
     * 
     */
    public HadroidTask getNextTask(){
        if(!chunkUndone.isEmpty()){//still some map work need to be done
//            HadroidTask
//            chunkUndone.
        }else if(chunkInProgress.isEmpty()){//all map work is done
            
        }
        return null;
    }
    
    private class FileChunk{
        public int chunkStartPos;
        public int chunkEndPos;
        
        public FileChunk(int chunkStartPos, int chunkEndPos) {
            this.chunkStartPos = chunkStartPos;
            this.chunkEndPos = chunkEndPos;
        }
        
        
    }
    
}
