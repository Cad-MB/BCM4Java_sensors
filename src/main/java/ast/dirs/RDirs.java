package ast.dirs;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class RDirs extends Dirs {
    Dir dir;
    Dirs dirs;

    public RDirs(Dir dir, Dirs dirs) {
        this.dir = dir;
        this.dirs = dirs;
    }

    @Override
    public Void eval(ExecutionStateI executionState) {
        // todo
//        ExecutionState eState = (ExecutionState) executionState;
        return null;
    }
}
