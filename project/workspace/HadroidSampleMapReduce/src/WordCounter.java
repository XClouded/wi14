
import java.util.List;

import uw.edu.hadroid.workflow.HadroidMapReduceJob;
import uw.edu.hadroid.workflow.MapFunction;
import uw.edu.hadroid.workflow.Pair;
import uw.edu.hadroid.workflow.ReduceFunction;


public class WordCounter extends HadroidMapReduceJob {

    public WordCounter(String filePath) {
        map = new WordCounterMap();
        reduce = new WordCounterReduce();
    }

    public class WordCounterMap extends MapFunction{

        @Override
        public List run(List input) {
            System.out.println("In MAP");
            return null;
        }
        
    }
    
    public class WordCounterReduce extends ReduceFunction{

        @Override
        public List run(List input) {
            // TODO Auto-generated method stub
            System.out.println("In REDUCE");
            return null;
        }
        
    }

    @Override
    public String getInputFilePath() {
        return "/Users/isphrazy/Documents/study/CSE/550/hw/wi14/project/workspace/data/sh.txt";
    }

    @Override
    public String getOutputFilePath() {
        return "/Users/isphrazy/Documents/study/CSE/550/hw/wi14/project/workspace/data/sh_out.txt";
    }
    
}
