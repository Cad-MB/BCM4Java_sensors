package components.client;

import ast.query.Query;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.ArrayList;

public class ClientPortForNode
    extends AbstractOutboundPort
    implements ClientCI {

    private static final long serialVersionUID = 1L;

    public ClientPortForNode(String uri, ComponentI owner) throws Exception {
        super(uri, ClientCI.class, owner);
    }

    public ClientPortForNode(ComponentI owner) throws Exception {
        super(ClientCI.class, owner);
    }

    @Override
    public ArrayList<String> sendRequestB(Query q) throws Exception {
        return ((ClientCI) this.getConnector()).sendRequestB(q);
    }

    @Override
    public ArrayList<SensorDataI> sendRequestG(Query q) throws Exception {
        return ((ClientCI) this.getConnector()).sendRequestG(q);
    }

}
