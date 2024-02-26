package components;

import components.node.NodeP2PInCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;

public class ConnectorNodeP2P
    extends AbstractConnector
    implements NodeP2PInCI {

    @Override
    public void connect(NodeInfoI neighbour) throws Exception {
        System.out.println("offering = " + offering);
        System.out.println("neighbour = " + neighbour);
        ((NodeP2PInCI) this.offering).connect(neighbour);
    }

    @Override
    public void disconnect(NodeInfoI neighbour) throws Exception {
        ((NodeP2PInCI) this.offering).disconnect(neighbour);
    }

}
