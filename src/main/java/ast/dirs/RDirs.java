package ast.dirs;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.Set;

public class RDirs extends Dirs {
    Direction dir;
    Dirs dirs;

    public RDirs(Direction dir, Dirs dirs) {
        this.dir = dir;
        this.dirs = dirs;
    }

    @Override
    public Set<Direction> eval(ExecutionStateI executionState) throws Exception {
        Set<Direction> directions = executionState.getDirections();
        directions.add(dir);
        Set<Direction> evaluatedDirections = dirs.eval(executionState);
        directions.addAll(evaluatedDirections);
        return directions;
    }
}
