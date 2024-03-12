package ast.dirs;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.Objects;
import java.util.Set;

public class FDirs
    implements Dirs {

    Direction dir;

    public FDirs(Direction dir) {
        this.dir = dir;
    }


    @Override
    public Set<Direction> eval(ExecutionStateI executionState) {
        Set<Direction> directions = executionState.getDirections();
        directions.add(dir);
        return directions;
    }

    @Override
    public String queryString() {
        return dir.toString();
    }

    @Override
    public String toString() {
        return "FDirs{dir=" + dir + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FDirs dirs = (FDirs) o;
        return dir == dirs.dir;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dir);
    }

}
