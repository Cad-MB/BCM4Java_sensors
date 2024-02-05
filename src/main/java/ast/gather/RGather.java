package ast.gather;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.HashMap;

public class RGather extends Gather<String, SensorDataI> {
    String sensorId;
    Gather<String, SensorDataI> gather;

    public RGather(String sensorId, Gather<String, SensorDataI> gather) {
        this.sensorId = sensorId;
        this.gather = gather;
    }

    @Override
    public HashMap<String, SensorDataI> eval(ExecutionStateI executionState) throws Exception {
        HashMap<String, SensorDataI> values = new HashMap<>();
        values.put(sensorId, executionState.getProcessingNode().getSensorData(sensorId));
        HashMap<String, SensorDataI> g = gather.eval(executionState);
        values.putAll(g);
        return values;
    }
}
