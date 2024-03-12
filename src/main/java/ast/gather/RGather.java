package ast.gather;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.HashMap;
import java.util.Objects;

/**
 * This class represents a recursive gather operation in the abstract syntax tree (AST).
 * It extends the Gather class and implements the eval method to evaluate the gather operation.
 */
public class RGather
    implements Gather {

    /**
     * The identifier of the sensor to gather.
     */
    String sensorId;
    /**
     * The gather operation to apply recursively.
     */
    Gather gather;

    /**
     * Constructor for the RecursiveGather class.
     *
     * @param sensorId The identifier of the sensor to gather.
     * @param gather   The gather operation to apply recursively.
     */
    public RGather(String sensorId, Gather gather) {
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

    @Override
    public String queryString() {
        return "@" + sensorId + " " + gather.queryString();
    }

    @Override
    public String toString() {
        return "RGather{sensorId='" + sensorId + '\'' + ", gather=" + gather + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RGather gather1 = (RGather) o;
        return Objects.equals(sensorId, gather1.sensorId) && Objects.equals(gather, gather1.gather);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensorId, gather);
    }

}
