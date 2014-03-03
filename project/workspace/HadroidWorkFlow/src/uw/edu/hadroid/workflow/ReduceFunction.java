package uw.edu.hadroid.workflow;

import java.util.List;

public abstract class ReduceFunction<IK, IV, OK, OV> extends HadroidFunction<Pair<IK, IV>, Pair<OK, OV>> {

    @Override
    abstract public List<Pair<OK, OV>> run(List<Pair<IK, IV>> input);

}
