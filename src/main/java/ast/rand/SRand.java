package ast.rand;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents a random operand associated with a sensor in the abstract syntax tree (AST).
 * It extends the Rand class and implements the eval method to return the sensor value.
 */
public class SRand
    implements Rand {

    /**
     * The identifier of the sensor associated with the random operand.
     */
    String sensorId;

    /**
     * Constructor for the SRand class.
     *
     * @param sensorId The identifier of the sensor associated with the random operand.
     */
    public SRand(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * Evaluates the random operand associated with a sensor by retrieving its current value.
     *
     * @param executionState The current execution state.
     * @return The current value of the sensor associated with the random operand.
     * @throws Exception If an error occurs while retrieving the sensor value.
     */
    @Override
    public Serializable eval(ExecutionStateI executionState) throws Exception {
        return executionState.getProcessingNode().getSensorData(sensorId).getValue();
    }

    @Override
    public String queryString() {
        return "@" + sensorId;
    }

    @Override
    public String toString() {
        return "SRand{sensorId='" + sensorId + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SRand rand = (SRand) o;
        return Objects.equals(sensorId, rand.sensorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensorId);
    }

}
