package sensor;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.io.Serializable;
import java.time.Instant;

public class SensorData<T extends Serializable> implements SensorDataI {
    String nodeId;
    String sensorId;
    T value;
    Instant timestamp;

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
        return this.getClass();
    }

    @Override
    public Serializable getValue() {
        return value;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }
}
