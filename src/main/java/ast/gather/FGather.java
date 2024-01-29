package ast.gather;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.HashMap;


public class FGather extends Gather<String, Object> {
    String sensorId;

    public FGather(String sensorId) {
        this.sensorId = sensorId;
    }

    @Override
    public HashMap<String, Object> eval(ExecutionStateI executionState) {
        HashMap<String, Object> result = new HashMap<>();
        result.put(sensorId, executionState.getProcessingNode().getSensorData(sensorId));
        return result;
    }
}
