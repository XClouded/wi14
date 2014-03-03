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

    public class WordCounterMap extends MapFunction<String, Integer>{

        @Override
        public List<Pair<String, Integer>> run(List<String> input) {
            System.out.println("In MAP");
            return null;
        }
        
    }
    
    public class WordCounterReduce extends ReduceFunction<String, Integer, String, Integer>{

        @Override
        public List<Pair<String, Integer>> run(List<Pair<String, Integer>> input) {
            // TODO Auto-generated method stub
            System.out.println("In REDUCE");
            return null;
        }
        
    }

    @Override
    public String getInputFilePath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getOutputFilePath() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
