package ast.cont;

import executionState.ExecutionState;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class ECont extends Cont {
    @Override
    public Void eval(ExecutionStateI executionState) throws Exception {
        assert executionState instanceof ExecutionState;
        ((ExecutionState) executionState).setDirectional(true);
        ((ExecutionState) executionState).setDirections(null);
        return null;
    }
}
