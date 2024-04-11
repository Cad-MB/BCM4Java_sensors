package components.node.outbound_ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;

public class NodeP2POutPort
    extends AbstractOutboundPort
    implements SensorNodeP2PCI {

    public NodeP2POutPort(String uri, ComponentI owner) throws Exception {
        super(uri, SensorNodeP2PCI.class, owner);
    }

    @Override
    public void ask4Connection(NodeInfoI neighbour) throws Exception {
        ((SensorNodeP2PCI) this.getConnector()).ask4Connection(neighbour);
    }

    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        ((SensorNodeP2PCI) this.getConnector()).ask4Disconnection(neighbour);
    }

    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        return ((SensorNodeP2PCI) this.getConnector()).execute(request);
    }

    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
        ((SensorNodeP2PCI) this.getConnector()).executeAsync(requestContinuation);
    }

}
