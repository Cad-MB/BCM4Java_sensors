package ast.cont;

import ast.dirs.Dirs;
import executionState.ExecutionState;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class DCont extends Cont {
    Dirs dirs;
    int nbSauts;

    public DCont(Dirs dirs, int nbSauts) {
        this.dirs = dirs;
        this.nbSauts = nbSauts;
    }

    @Override
    public Void eval(ExecutionStateI executionState) throws Exception {
        assert executionState instanceof ExecutionState;
        ((ExecutionState) executionState).setDirectional(true);
        ((ExecutionState) executionState).setDirections(dirs.eval(executionState));
        return null;
    }
}
