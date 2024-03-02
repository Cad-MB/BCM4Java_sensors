package ast.bexp;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class SBExp
    implements BExp {

    String sensorId;

    public SBExp(String sensorId) {
        this.sensorId = sensorId;
    }

    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        SensorDataI sensorData = executionState.getProcessingNode().getSensorData(sensorId);
        assert sensorData.getType() == Boolean.class;
        return (Boolean) sensorData.getValue();
    }

}
