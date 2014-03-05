
import java.util.List;

import uw.edu.hadroid.workflow.HadroidMapReduceJob;
import uw.edu.hadroid.workflow.Pair;


public class WordCounter extends HadroidMapReduceJob {

    public WordCounter() {
        map = new WordCounterMap();
        reduce = new WordCounterReduce();
    }


    @Override
    public String getInputFilePath() {
        return "/Users/isphrazy/Documents/study/CSE/550/hw/wi14/project/workspace/data/sh_ascii.txt";
    }

    @Override
    public String getOutputFilePath() {
        return "/Users/isphrazy/Documents/study/CSE/550/hw/wi14/project/workspace/data/sh_out.txt";
    }
    
}
