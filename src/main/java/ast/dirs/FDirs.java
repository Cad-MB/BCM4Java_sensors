package ast.dirs;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class FDirs extends Dirs {
    Dir dir;

    public FDirs(Dir dir) {
        this.dir = dir;
    }

    @Override
    public Void eval(ExecutionStateI executionState) {
        // todo
        return null;
    }
}
