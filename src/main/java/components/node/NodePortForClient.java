package components.node;

import components.ConnectorNodeClient;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;

public class NodePortForClient
    extends AbstractOutboundPort
    implements RequestResultCI {

    public NodePortForClient(String uri, ComponentI owner) throws Exception {
        super(uri, RequestResultCI.class, owner);
    }

    @Override
    public void acceptRequestResult(String s, QueryResultI i) throws Exception {
        ((ConnectorNodeClient) this.getConnector()).acceptRequestResult(s, i);
    }

}
