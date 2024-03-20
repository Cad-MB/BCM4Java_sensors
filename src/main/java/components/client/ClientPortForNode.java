package components.client;

import components.ConnectorClientNode;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;

public class ClientPortForNode
    extends AbstractOutboundPort
    implements ClientNodeOutCI {

    private static final long serialVersionUID = 1L;

    public ClientPortForNode(String uri, ComponentI owner) throws Exception {
        super(uri, ClientNodeOutCI.class, owner);
    }

    public ClientPortForNode(ComponentI owner) throws Exception {
        super(ClientNodeOutCI.class, owner);
    }

    @Override
    public QueryResultI sendRequest(RequestI r) throws Exception {
        return ((ConnectorClientNode) this.getConnector()).sendRequest(r);
    }

    @Override
    public void sendAsyncRequest(RequestI req) throws Exception {
        ((ConnectorClientNode) this.getConnector()).sendAsyncRequest(req);
    }

}
