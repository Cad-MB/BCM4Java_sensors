package components.client.inbound_ports;

import components.client.Client;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;

public class ClientReqResultInPort
    extends AbstractInboundPort
    implements RequestResultCI {

    public ClientReqResultInPort(String uri, ComponentI owner) throws Exception {
        super(uri, RequestResultCI.class, owner);
    }

    @Override
    public void acceptRequestResult(String requestUri, QueryResultI res) throws Exception {
        this.getOwner().handleRequest(owner -> ((Client) owner)).acceptQueryResult(requestUri, res);
    }

}
