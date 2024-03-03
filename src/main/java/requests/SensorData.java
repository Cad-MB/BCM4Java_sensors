package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class SensorData<T extends Serializable>
    implements SensorDataI {

    String nodeId; // The identifier of the sensor node that collected the data
    String sensorId; // The identifier of the sensor that collected the data
    T value; // The value of the collected data
    Instant timestamp; // The timestamp indicating when the data was collected

    /**
     * Constructs a {@code SensorData} object with the given node ID, sensor ID, value, and timestamp.
     *
     * @param nodeId    the identifier of the sensor node
     * @param sensorId  the identifier of the sensor
     * @param value     the value of the collected data
     * @param timestamp the timestamp when the data was collected
     */
    public SensorData(String nodeId, String sensorId, T value, Instant timestamp) {
        this.nodeId = nodeId;
        this.sensorId = sensorId;
        this.value = value;
        this.timestamp = timestamp;
    }

    @Override
    public String getNodeIdentifier() {
        return nodeId;
    }

    @Override
    public String getSensorIdentifier() {
        return sensorId;
    }

    @Override
    public Class<? extends Serializable> getType() {
        return value.getClass();
    }

    @Override
    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "SensorData{" +
               "nodeId='" + nodeId + '\'' +
               ", sensorId='" + sensorId + '\'' +
               ", value=" + value +
               ", timestamp=" + timestamp +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SensorData<?> data = (SensorData<?>) o;
        return Objects.equals(nodeId, data.nodeId) &&
               Objects.equals(sensorId, data.sensorId) &&
               Objects.equals(value, data.value) &&
               Objects.equals(timestamp, data.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, sensorId, value, timestamp);
    }

}
