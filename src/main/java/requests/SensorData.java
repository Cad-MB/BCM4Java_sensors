package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class SensorData<T extends Serializable>
    implements SensorDataI {

    String nodeId; // L'identifiant du nœud de capteur qui a collecté les données
    String sensorId; // L'identifiant du capteur qui a collecté les données
    T value; // La valeur des données collectées
    Instant timestamp; // Le timestamp indiquant le moment où les données ont été collectées

    // Constructeur pour initialiser les données du capteur
    public SensorData(String nodeId, String sensorId, T value, Instant timestamp) {
        this.nodeId = nodeId;
        this.sensorId = sensorId;
        this.value = value;
        this.timestamp = timestamp;
    }

    // Méthode pour obtenir l'identifiant du nœud de capteur
    @Override
    public String getNodeIdentifier() {
        return nodeId;
    }

    // Méthode pour obtenir l'identifiant du capteur
    @Override
    public String getSensorIdentifier() {
        return sensorId;
    }

    // Méthode pour obtenir le type de données collectées
    @Override
    public Class<? extends Serializable> getType() {
        return value.getClass();
    }

    // Méthode pour obtenir la valeur des données collectées
    @Override
    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    // Méthode pour obtenir le timestamp des données collectées
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
