package ast.gather;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.HashMap;
import java.util.Objects;

/**
 * This class represents a flood gather operation in the abstract syntax tree (AST).
 * It extends the Gather class and implements the eval method to evaluate the gather operation.
 */
public class FGather
    implements Gather {

    /**
     * The identifier of the sensor to gather.
     */
    String sensorId;

    /**
     * Constructor for the FloodGather class.
     *
     * @param sensorId The identifier of the sensor to gather.
     */
    public FGather(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * Evaluates the flood gather operation.
     *
     * @param executionState The current execution state.
     * @return A map containing the data collected by this operation.
     */
    @Override
    public HashMap<String, SensorDataI> eval(ExecutionStateI executionState) {
        HashMap<String, SensorDataI> result = new HashMap<>();
        // Collects data from the specified sensor and adds it to the resulting map
        result.put(sensorId, executionState.getProcessingNode().getSensorData(sensorId));
        return result; // Returns the map containing the collected data
    }

    @Override
    public String queryString() {
        return "@" + sensorId;
    }

    @Override
    public String toString() {
        return "FGather{sensorId='" + sensorId + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FGather gather = (FGather) o;
        return Objects.equals(sensorId, gather.sensorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensorId);
    }

}
