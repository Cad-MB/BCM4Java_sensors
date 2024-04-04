package ast.cont;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import requests.ExecutionState;

import java.util.HashSet;

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
        ((ExecutionState) executionState).setDirectionalState(0, new HashSet<>());
        return null;
    }


    @Override
    public String queryString() {
        return "(empty)";
    }

    @Override
    public String toString() {
        return "ECont{}";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ECont;
    }

}
