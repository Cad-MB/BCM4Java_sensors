package ast.cont;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import requests.ExecutionState;

/**
 * This class represents an empty continuation in the abstract syntax tree (AST) of queries.
 * It extends the Cont class and implements the eval method to configure the execution state as directional without any specified direction.
 */
public class ECont
    implements Cont {

    /**
     * Configures the execution state as directional without any specified direction.
     *
     * @param executionState The current execution state.
     * @return Null because this method does not return any specific result.
     * @throws Exception If an error occurs while configuring the execution state.
     */
    @Override
    public Void eval(ExecutionStateI executionState) throws Exception {
        assert executionState instanceof ExecutionState;
        ExecutionState es = (ExecutionState) executionState;
        es.setDirectional(true);
        es.setDirections(null);
        es.setNbHops(0);
        return null;
    }

}
