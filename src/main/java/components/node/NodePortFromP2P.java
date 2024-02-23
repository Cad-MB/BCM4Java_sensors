package components.node;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;

public class NodePortFromP2P extends AbstractInboundPort implements NodeP2PInCI {

    public NodePortFromP2P(String uri, ComponentI owner) throws Exception {
        super(uri, NodeP2PInCI.class, owner);
        assert owner instanceof Node;
    }

    @Override
    public void connect(NodeInfoI neighbour) throws Exception {
        this.getOwner().handleRequest(c -> {
            ((Node) c).connect(neighbour);
            return null;
        });
    }

    @Override
    public void disconnect(NodeInfoI neighbour) throws Exception {
        this.getOwner().handleRequest(c -> {
            ((Node) c).disconnect(neighbour);
            return null;
        });
    }
}
