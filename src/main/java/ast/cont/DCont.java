package ast.cont;

import ast.dirs.Dirs;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class DCont extends Cont {
    Dirs dirs;
    int nbSauts;

    public DCont(Dirs dirs, int nbSauts) {
        this.dirs = dirs;
        this.nbSauts = nbSauts;
    }

    @Override
    public Void eval(ExecutionStateI executionState) {
        executionState.getDirections();
        return null;
    }
}
