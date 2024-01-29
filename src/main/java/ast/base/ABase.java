package ast.base;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class ABase extends Base {
    PositionI position;

    public ABase(PositionI position) {
        this.position = position;
    }

    @Override
    public PositionI eval(ExecutionStateI executionState) {
        return position;
    }
}
