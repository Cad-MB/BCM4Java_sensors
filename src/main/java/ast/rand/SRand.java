package ast.rand;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class SRand extends Rand {
    String sensorId;

    public SRand(String sensorId) {
        this.sensorId = sensorId;
    }

    @Override
    public Double eval(ExecutionStateI executionState) throws Exception {
        // todo
        return (Double) executionState.getProcessingNode().getSensorData(sensorId).getValue();
    }
}
