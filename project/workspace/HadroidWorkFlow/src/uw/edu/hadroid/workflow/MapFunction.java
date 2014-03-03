package uw.edu.hadroid.workflow;

import java.util.List;

/**
 * 
 * @author isphrazy
 *
 * @param <K> type of return key
 * @param <V> type of return value
 */
public abstract class MapFunction extends HadroidFunction {

    @Override
    abstract public List run(List input);

}
