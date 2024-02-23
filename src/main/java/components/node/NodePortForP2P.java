package components.node;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;

public class NodePortForP2P extends AbstractOutboundPort implements NodeP2POutCI {

    public NodePortForP2P(String uri, ComponentI owner) throws Exception {
        super(uri, NodeP2POutCI.class, owner);
        assert owner instanceof Node;
    }

    @Override
    public void ask4Connection(NodeInfoI neighbour) throws Exception {
        ((NodeP2PInCI) this.getConnector()).connect(neighbour);
    }

    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        ((NodeP2PInCI) this.getConnector()).disconnect(neighbour);
    }

    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        return null;
    }

    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {

    }
}
