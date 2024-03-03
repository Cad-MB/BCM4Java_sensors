package ast.cont;

import ast.base.Base;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import requests.ExecutionState;

/**
 * This class represents a flood continuation in the abstract syntax tree (AST) of queries.
 * It extends the Cont class and implements the eval method to configure the execution state with the specified maximum scope.
 */
public class FCont
    implements Cont {

    Base base;
    /**
     * The maximum distance from the specified base in the flood continuation.
     */
    double distance;

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
        base.eval(executionState);
        assert executionState instanceof ExecutionState;
        ExecutionState es = (ExecutionState) executionState;
        es.setFlooding(true);
        es.setMaxDistance(distance);
        return null;
    }

}
