package ast.dirs;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.Objects;
import java.util.Set;

/**
 * This class represents a list of directions with an additional direction followed by another list of directions in the abstract syntax tree (AST) of queries.
 * It extends the Dirs class and implements the eval method to add the specified direction as well as the evaluated directions from the next list to the current set of directions.
 */
public class RDirs
    implements Dirs {

    /**
     * The direction to add to the set of directions.
     */
    Direction dir;
    /**
     * The next list of directions to evaluate.
     */
    Dirs dirs;

    /**
     * Constructor for the RDirs class.
     *
     * @param dir  The direction to add to the set of directions.
     * @param dirs The next list of directions to evaluate.
     */
    public RDirs(Direction dir, Dirs dirs) {
        this.dir = dir;
        this.dirs = dirs;
    }

    /**
     * Adds the specified direction as well as the evaluated directions from the next list to the current set of directions.
     *
     * @param executionState The current execution state.
     * @return The updated set of directions with the specified direction and the evaluated directions from the next list.
     * @throws Exception If an error occurs while evaluating the directions from the next list.
     */
    @Override
    public Set<Direction> eval(ExecutionStateI executionState) throws Exception {
        Set<Direction> directions = executionState.getDirections();
        // Adds the specified direction to the set of directions
        directions.add(dir);
        // Evaluates the directions from the next list and adds them to the current set of directions
        Set<Direction> evaluatedDirections = dirs.eval(executionState);
        directions.addAll(evaluatedDirections);
        return directions;
    }

    @Override
    public String queryString() {
        return dir + " " + dirs.queryString();
    }

    @Override
    public String toString() {
        return "RDirs{dir=" + dir + ", dirs=" + dirs + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RDirs dirs1 = (RDirs) o;
        return dir == dirs1.dir && Objects.equals(dirs, dirs1.dirs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dir, dirs);
    }

}
