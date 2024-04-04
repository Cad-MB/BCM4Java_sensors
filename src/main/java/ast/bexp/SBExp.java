package ast.bexp;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.Objects;

/**
 * This class represents a sensor-based boolean expression in the abstract syntax tree (AST).
 * It implements the BExp interface and evaluates the expression based on sensor data.
 */
public class SBExp
    implements BExp {

    /**
     * The ID of the sensor.
     */
    protected String sensorId;

    /**
     * Constructs an SBExp object with the given sensor ID.
     *
     * @param sensorId The ID of the sensor.
     */
    public SBExp(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * Evaluates the sensor-based boolean expression.
     *
     * @param executionState The current execution state.
     * @return The result of evaluating the sensor-based boolean expression.
     * @throws Exception If an error occurs during the evaluation.
     */
    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        return (Boolean) executionState.getProcessingNode().getSensorData(sensorId).getValue();
    }

    @Override
    public String queryString() {
        return "@" + sensorId;
    }

    @Override
    public String toString() {
        return "SBExp{sensorId='" + sensorId + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SBExp exp = (SBExp) o;
        return Objects.equals(sensorId, exp.sensorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensorId);
    }

}
