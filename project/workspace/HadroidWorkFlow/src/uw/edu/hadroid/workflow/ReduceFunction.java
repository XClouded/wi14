package uw.edu.hadroid.workflow;

import java.util.List;

public abstract class ReduceFunction extends HadroidFunction {

    @Override
    abstract public List run(List input);

}
