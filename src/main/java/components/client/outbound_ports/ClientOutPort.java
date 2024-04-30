package components.client.outbound_ports;

import components.ConnectorClientNode;
import components.client.ClientCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;

public class ClientOutPort
    extends AbstractOutboundPort
    implements ClientCI {

    public ClientOutPort(String uri, ComponentI owner) throws Exception {
        super(uri, ClientCI.class, owner);
    }

    @Override
    public QueryResultI sendRequest(RequestI r) throws Exception {
        return ((ConnectorClientNode) this.getConnector()).execute(r);
    }

    @Override
    public void sendAsyncRequest(RequestI req) throws Exception {
        ((ConnectorClientNode) this.getConnector()).executeAsync(req);
    }

}
