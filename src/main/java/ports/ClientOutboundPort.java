package ports;

import ast.query.Query;
import components.interfaces.ClientCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;

import java.util.ArrayList;

public class ClientOutboundPort
    extends AbstractOutboundPort
    implements ClientCI
{
    private static final long serialVersionUID = 1L;
    public ClientOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, ClientCI.class, owner);
    }

    public ClientOutboundPort(ComponentI owner) throws Exception {
        super(ClientCI.class, owner);
    }

    @Override
    public ArrayList<String> sendRequest(Query q) throws Exception
    {
        return ((ClientCI)this.getConnector()).sendRequest(q);
    }
}
