package ast.cont;

import executionState.ExecutionState;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class FCont extends Cont {
    double distance;

    public FCont(double distance) {
        this.distance = distance;
    }

    @Override
    public Void eval(ExecutionStateI executionState) throws Exception {
        assert executionState instanceof ExecutionState;
        ((ExecutionState) executionState).setFlooding(true);
        ((ExecutionState) executionState).setMaxDistance(distance);
        return null;
    }
}
