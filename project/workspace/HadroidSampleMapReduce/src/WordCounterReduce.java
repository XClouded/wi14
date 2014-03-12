import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uw.edu.hadroid.workflow.HadroidFunction;
import uw.edu.hadroid.workflow.Pair;

public class WordCounterReduce  extends HadroidFunction {
    @Override
    public List run(List input) {
        if(input == null) {
            System.err.println("input to reduce function is null");
            return null;
        }
        List<Pair<String, Integer>> result = new LinkedList<Pair<String, Integer>>();//key word to count
        Map<String, Integer> keyToCount = new HashMap<String, Integer>();
        for(Object o : input){
            if(!(o instanceof Pair)){
                System.err.println("input element is not Pair");
                return null;
            }
            Pair p = (Pair)o;
            if(!(p.key instanceof String)){
                System.err.println("input key is not String");
                return null;
            }
            if(!(p.value instanceof String)){
                System.err.println("input value is not String");
                return null;
            }
            if(keyToCount.containsKey((String)p.key)){
                keyToCount.put((String)p.key, keyToCount.get(p.key) + Integer.parseInt((String)p.value));
            }else{
                keyToCount.put((String)p.key, Integer.parseInt((String)p.value));
            }
        }
        for(String key : keyToCount.keySet()){
            result.add(new Pair<String, Integer>(key, keyToCount.get(key)));
        }
        return result;
    }
}
