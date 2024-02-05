package ast.dirs;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.Set;

public class FDirs extends Dirs {
    Direction dir;

    public FDirs(Direction dir) {
        this.dir = dir;
    }

    @Override
    public Set<Direction> eval(ExecutionStateI executionState) {
        // todo
        return null;
    }
}
