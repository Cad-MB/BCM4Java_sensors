package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;

import java.util.Set;

public class ProcessingNode
    implements ProcessingNodeI {

    String id;
    PositionI position;
    Set<NodeInfoI> neighbours;
    Set<SensorDataI> sensorData;

    public ProcessingNode(String id, PositionI position, Set<NodeInfoI> neighbours, Set<SensorDataI> sensorData) {
        this.id = id;
        this.position = position;
        this.neighbours = neighbours;
        this.sensorData = sensorData;
    }

    @Override
    public String getNodeIdentifier() {
        return id;
    }

    @Override
    public PositionI getPosition() {
        return position;
    }

    @Override
    public Set<NodeInfoI> getNeighbours() {
        return neighbours;
    }

    @Override
    public SensorDataI getSensorData(String s) {
        return sensorData
            .stream()
            .filter(data -> data.getSensorIdentifier().equals(s))
            .findFirst()
            .orElse(null);
    }

}
