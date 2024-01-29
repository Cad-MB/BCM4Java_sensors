package ast.base;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class RBase extends Base {

    @Override
    public PositionI eval(ExecutionStateI executionState) {
        return executionState.getProcessingNode().getPosition();
    }
}
