package ast.gather;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.HashMap;

public class RGather extends Gather<String, Object> {
    String sensorId;
    Gather<String, Object> gather;

    public RGather(String sensorId, Gather<String, Object> gather) {
        this.sensorId = sensorId;
        this.gather = gather;
    }

    @Override
    public HashMap<String, Object> eval(ExecutionStateI executionState) throws Exception {
        HashMap<String, Object> values = new HashMap<>();
        values.put(sensorId, executionState.getProcessingNode().getSensorData(sensorId));
        HashMap<String, Object> g = gather.eval(executionState);
        values.putAll(g);
        return values;
    }
}
