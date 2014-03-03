package uw.edu.hadroid.workflow;

import java.util.List;

/**
 * 
 * @author isphrazy
 *
 * @param <K> type of return key
 * @param <V> type of return value
 */
public abstract class MapFunction<K, V> extends HadroidFunction<String, Pair<K, V>> {

    @Override
    abstract public List<Pair<K, V>> run(List<String> input);

}
