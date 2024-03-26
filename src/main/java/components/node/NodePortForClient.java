package components.node;

import components.ConnectorNodeClient;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;

public class NodePortForClient
    extends AbstractOutboundPort
    implements NodeClientOutCI {

    public NodePortForClient(String uri, ComponentI owner) throws Exception {
        super(uri, NodeClientOutCI.class, owner);
    }

    @Override
    public void sendQueryResult(String requestURI, QueryResultI result) throws Exception {
        System.out.println("NodePortForClient.sendQueryResult");
        ((ConnectorNodeClient) this.getConnector()).sendQueryResult(requestURI, result);
    }

}
