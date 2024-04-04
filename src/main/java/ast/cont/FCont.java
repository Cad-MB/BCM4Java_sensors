package ast.cont;

import ast.base.Base;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import sensor_network.requests.ExecutionState;

import java.util.Objects;

/**
 * This class represents a flood continuation in the abstract syntax tree (AST) of queries.
 * It extends the Cont class and implements the eval method to configure the execution state with the specified maximum scope.
 */
public class FCont
    implements Cont {

    protected Base base;
    /**
     * The maximum distance from the specified base in the flood continuation.
     */
    protected double distance;

    /**
     * Constructor for the FCont class.
     *
     * @param base     The base from which the flood continuation starts.
     * @param distance The maximum distance from the specified base in the flood continuation.
     */
    public FCont(Base base, double distance) {
        this.base = base;
        this.distance = distance;
    }

    /**
     * Configures the execution state with the specified maximum scope in the flood continuation.
     *
     * @param executionState The current execution state.
     * @return Null because this method does not return any specific result.
     * @throws Exception If an error occurs while configuring the execution state.
     */
    @Override
    public Void eval(ExecutionStateI executionState) throws Exception {
        PositionI pos = base.eval(executionState);
        assert executionState instanceof ExecutionState;
        ((ExecutionState) executionState).setFloodingState(pos, distance);
        return null;
    }

    @Override
    public String queryString() {
        return "(flood " + base.queryString() + " " + distance + ')';
    }

    @Override
    public String toString() {
        return "FCont{base=" + base + ", distance=" + distance + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FCont cont = (FCont) o;
        return Double.compare(distance, cont.distance) == 0 && Objects.equals(base, cont.base);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, distance);
    }

}
