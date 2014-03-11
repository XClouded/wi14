import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uw.edu.hadroid.workflow.HadroidFunction;
import uw.edu.hadroid.workflow.Pair;


public class WordCounterMap extends HadroidFunction {
    
    @Override
    public List run(List input) {
        List<Pair<String, Integer>> result = new LinkedList<Pair<String, Integer>>();
        Map<String, Integer> wordCount = new HashMap<String, Integer>();
        for(Object o : input){
            if(!(o instanceof String)){
                System.err.println("input element is not string");
                return null;
            }
            String word = (String)o;
            if(wordCount.containsKey(word)){
                wordCount.put(word, wordCount.get(word) + 1);
            }else{
                wordCount.put(word, 1);
            }
        }
        for(String word : wordCount.keySet()){
            result.add(new Pair<String, Integer>(word, wordCount.get(word)));
        }
        return result;
    }
}
