package ast.gather;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.HashMap;

/**
 * This class represents a recursive gather operation in the abstract syntax tree (AST).
 * It extends the Gather class and implements the eval method to evaluate the gather operation.
 */
public class RGather
    implements Gather<String, SensorDataI> {

    /**
     * The identifier of the sensor to gather.
     */
    String sensorId;
    /**
     * The gather operation to apply recursively.
     */
    Gather<String, SensorDataI> gather;

    /**
     * Constructor for the RecursiveGather class.
     *
     * @param sensorId The identifier of the sensor to gather.
     * @param gather   The gather operation to apply recursively.
     */
    public RGather(String sensorId, Gather<String, SensorDataI> gather) {
        this.sensorId = sensorId;
        this.gather = gather;
    }

    /**
     * Evaluates the recursive gather operation.
     *
     * @param executionState The current execution state.
     * @return A map containing the data collected by this operation and those collected by the recursive operation.
     * @throws Exception If an error occurs while evaluating the recursive operation.
     */
    @Override
    public HashMap<String, SensorDataI> eval(ExecutionStateI executionState) throws Exception {
        HashMap<String, SensorDataI> values = new HashMap<>();
        // Collects data from the specified sensor
        values.put(sensorId, executionState.getProcessingNode().getSensorData(sensorId));
        // Evaluates the recursive gather operation and adds its results to the map
        HashMap<String, SensorDataI> recursiveGather = gather.eval(executionState);
        values.putAll(recursiveGather);
        return values;
    }

}
