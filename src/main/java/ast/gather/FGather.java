package ast.gather;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.HashMap;


public class FGather extends Gather<String, SensorDataI> {
    String sensorId;

    public FGather(String sensorId) {
        this.sensorId = sensorId;
    }

    @Override
    public HashMap<String, SensorDataI> eval(ExecutionStateI executionState) {
        HashMap<String, SensorDataI> result = new HashMap<>();
        result.put(sensorId, executionState.getProcessingNode().getSensorData(sensorId));
        return result;
    }
}
