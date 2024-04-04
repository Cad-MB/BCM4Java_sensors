package sensor_network.requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.ArrayList;

/**
 * This class represents the result of a query in a sensor network.
 * It implements the {@link fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI} interface.
 */
public class QueryResult
    implements QueryResultI {

    /*
     * Indicates whether the query is a boolean type or a data collection type
     */
    protected final boolean isBoolean;

    /*
     * List of sensor nodes responding positively to the boolean query
     */
    protected final ArrayList<String> positiveSensorNodes;

    /*
     * List of sensor data collected in response to the collection query
     */
    protected final ArrayList<SensorDataI> gatheredSensorValues;

    /**
     * Constructs a {@code QueryResult} object for a boolean query.
     *
     * @param isBoolean            true if the query is boolean, false otherwise
     * @param positiveSensorNodes  list of sensor nodes responding positively to the boolean query
     * @param gatheredSensorValues list of collected sensor data in response to the collection query
     */
    public QueryResult(
        boolean isBoolean, ArrayList<String> positiveSensorNodes, ArrayList<SensorDataI> gatheredSensorValues
    ) {
        this.isBoolean = isBoolean;
        this.positiveSensorNodes = positiveSensorNodes;
        this.gatheredSensorValues = gatheredSensorValues;
    }

    /**
     * Constructs a {@code QueryResult} object for a boolean query without positive sensor nodes or gathered sensor data.
     *
     * @param isBoolean true if the query is boolean, false otherwise
     */
    public QueryResult(boolean isBoolean) {
        this.isBoolean = isBoolean;
        this.positiveSensorNodes = new ArrayList<>();
        this.gatheredSensorValues = new ArrayList<>();
    }

    /**
     * Checks if the query is a boolean type.
     *
     * @return true if the query is boolean, false otherwise
     */
    @Override
    public boolean isBooleanRequest() {
        return isBoolean;
    }

    /**
     * Gets the list of sensor nodes responding positively to the boolean query.
     *
     * @return the list of sensor nodes responding positively to the boolean query
     */
    @Override
    public ArrayList<String> positiveSensorNodes() {
        return positiveSensorNodes;
    }

    /**
     * Checks if the query is a data collection type.
     *
     * @return true if the query is a data collection type, false otherwise
     */
    @Override
    public boolean isGatherRequest() {
        return !isBoolean;
    }

    /**
     * Gets the list of collected sensor data in response to the collection query.
     *
     * @return the list of collected sensor data in response to the collection query
     */
    @Override
    public ArrayList<SensorDataI> gatheredSensorsValues() {
        return gatheredSensorValues;
    }

    /**
     * Adds a sensor value to the list of collected sensor data.
     *
     * @param sensorValue the sensor value to add
     */
    public void addSensorValue(SensorDataI sensorValue) {
        gatheredSensorValues.add(sensorValue);
    }

    /**
     * Adds a sensor node to the list of sensor nodes responding positively to the boolean query.
     *
     * @param nodeId the ID of the sensor node to add
     */
    public void addPositiveNode(String nodeId) {
        positiveSensorNodes.add(nodeId);
    }

    /**
     * Returns a string representation of the query result.
     *
     * @return a string representation of the query result
     */
    @Override
    public String toString() {
        return "QueryResult{" +
               "isBoolean=" + isBoolean +
               (isBoolean
                    ? ", positiveSensorNodes=" + positiveSensorNodes
                    : ", gatheredSensorValues=" + gatheredSensorValues) +
               '}';
    }

}
