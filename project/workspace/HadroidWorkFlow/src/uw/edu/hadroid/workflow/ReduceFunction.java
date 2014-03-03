package uw.edu.hadroid.workflow;

import java.io.Serializable;
import java.util.List;

public abstract class ReduceFunction extends HadroidFunction implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1405096011968470140L;

    @Override
    abstract public List run(List input);

}
