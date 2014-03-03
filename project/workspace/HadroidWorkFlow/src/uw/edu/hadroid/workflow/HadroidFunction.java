package uw.edu.hadroid.workflow;

import java.util.List;

public abstract class HadroidFunction<I, O> {

    abstract public List<O> run(List<I> input);

}
