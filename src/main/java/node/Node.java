package node;

import fr.sorbonne_u.cps.sensor_network.interfaces.*;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;

import java.util.Set;

public class Node implements RequestingCI, NodeInfoI, ProcessingNodeI {
    double range;
    String id;
    PositionI position;


    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return null;
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {

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
        return null;
    }

    @Override
    public PositionI getPosition() {
        return null;
    }

    @Override
    public Set<NodeInfoI> getNeighbours() {
        return null;
    }

    @Override
    public SensorDataI getSensorData(String sensorIdentifier) {
        return null;
    }

    @Override
    public QueryResultI propagateRequest(String nodeIdentifier, RequestContinuationI requestContinuation) throws Exception {
        return null;
    }

    @Override
    public void propagateRequestAsync(String nodeIdentifier, RequestContinuationI requestContinuation) throws Exception {

    }
}
