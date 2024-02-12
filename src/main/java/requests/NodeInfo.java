package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;

import java.util.Map;
import java.util.Set;

public class NodeInfo implements NodeInfoI, ProcessingNodeI {
    double range;
    String id;
    PositionI position;
    public Map<String, SensorData<Double>> sensors;
    private Set<NodeInfoI> neighbours;

    public NodeInfo(int i, String node1) {
        this.id = node1;
        this.range = i;
    }

    @Override
    public PositionI nodePosition() {
        return position;
    }

    @Override
    public double nodeRange() {
        return range;
    }

    @Override
    public EndPointDescriptorI p2pEndPointInfo() {
        return null;
    }

    @Override
    public String nodeIdentifier() {
        return id;
    }

    @Override
    public EndPointDescriptorI endPointInfo() {
        return null;
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

    public void setNeighbours(Set<NodeInfoI> newNeighbours) {
        neighbours = newNeighbours;
    }

    @Override
    public SensorDataI getSensorData(String sensorIdentifier) {
        return sensors.get(sensorIdentifier);
    }

}