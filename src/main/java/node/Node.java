package node;

import fr.sorbonne_u.cps.sensor_network.interfaces.*;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import sensor.SensorData;

import java.util.Map;
import java.util.Set;

public class Node implements NodeInfoI, ProcessingNodeI {
    double range;
    String id;
    PositionI position;
    Map<String, SensorData> sensors;

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
        return null;
    }

    @Override
    public SensorDataI getSensorData(String sensorIdentifier) {
        return sensors.get(sensorIdentifier);
    }

    @Override
    public QueryResultI propagateRequest(String nodeIdentifier, RequestContinuationI requestContinuation) throws Exception {
        return null;
    }

    @Override
    public void propagateRequestAsync(String nodeIdentifier, RequestContinuationI requestContinuation) throws Exception {

    }
}