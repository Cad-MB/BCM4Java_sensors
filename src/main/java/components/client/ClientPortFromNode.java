package components.client;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;

public class ClientPortFromNode
    extends AbstractInboundPort
    implements ClientNodeInCI{


    public ClientPortFromNode(String uri, ComponentI owner) throws Exception {
        super(uri, ClientNodeInCI.class, owner);
    }

    @Override
    public void acceptRequestResult(String requestUri, QueryResultI res) throws Exception {
        // todo check
        this.getOwner().handleRequest(owner-> ((Client) owner)).acceptQueryResult(requestUri, res);
    }

}
