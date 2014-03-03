package uw.edu.hadroid.workflow;

import java.io.Serializable;
import java.util.List;

public abstract class HadroidFunction implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -4374401134567200523L;

    abstract public List run(List input);

}
