package sensor_network.requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;

import java.util.Set;

/**
 * This class represents a processing node in a sensor network.
 * It implements the {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI} interface.
 */
public class ProcessingNode
    implements ProcessingNodeI {

    protected String id;
    protected PositionI position;
    protected Set<NodeInfoI> neighbours;
    protected Set<SensorDataI> sensorData;

    /**
     * Constructs a {@code ProcessingNode} object with the given ID, position, neighbours, and sensor data.
     *
     * @param id         the ID of the processing node
     * @param position   the position of the processing node
     * @param neighbours the set of neighbouring nodes
     * @param sensorData the set of sensor data associated with the node
     */
    public ProcessingNode(String id, PositionI position, Set<NodeInfoI> neighbours, Set<SensorDataI> sensorData) {
        this.id = id;
        this.position = position;
        this.neighbours = neighbours;
        this.sensorData = sensorData;
    }

    /**
     * Gets the ID of the processing node.
     *
     * @return the ID of the processing node
     */
    @Override
    public String getNodeIdentifier() {
        return id;
    }

    /**
     * Gets the position of the processing node.
     *
     * @return the position of the processing node
     */
    @Override
    public PositionI getPosition() {
        return position;
    }

    /**
     * Gets the set of neighbouring nodes.
     *
     * @return the set of neighbouring nodes
     */
    @Override
    public Set<NodeInfoI> getNeighbours() {
        return neighbours;
    }

    /**
     * Gets the sensor data associated with the specified sensor identifier.
     *
     * @param s the sensor identifier
     * @return the sensor data associated with the specified sensor identifier, or {@code null} if not found
     */
    @Override
    public SensorDataI getSensorData(String s) {
        return sensorData
            .stream()
            .filter(data -> data.getSensorIdentifier().equals(s))
            .findFirst()
            .orElse(null);
    }

}
