package ast.bexp;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class SBExp
    implements BExp {

    /**
     * The ID of the sensor.
     */
    String sensorId;

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
        SensorDataI sensorData = executionState.getProcessingNode().getSensorData(sensorId);
        assert sensorData.getType() == Boolean.class; // Make sure the sensor data type is Boolean
        return (Boolean) sensorData.getValue(); // Return the Boolean value of the sensor data
    }

}
