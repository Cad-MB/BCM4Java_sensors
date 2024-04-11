package components.node.outbound_ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;

public class NodeReqResultOutPort
    extends AbstractOutboundPort
    implements RequestResultCI {

    public NodeReqResultOutPort(String uri, ComponentI owner) throws Exception {
        super(uri, RequestResultCI.class, owner);
    }

    @Override
    public void acceptRequestResult(String s, QueryResultI i) throws Exception {
        ((RequestResultCI) this.getConnector()).acceptRequestResult(s, i);
    }

}
