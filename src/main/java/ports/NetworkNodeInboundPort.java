package ports;

import ast.query.Query;
import components.NetworkNode;
import components.interfaces.NetworkNodeCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

import java.util.ArrayList;

public class NetworkNodeInboundPort
    extends AbstractInboundPort
    implements NetworkNodeCI
{
    private static final long serialVersionUID = 1L;
    public NetworkNodeInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, NetworkNodeCI.class, owner);
        assert owner instanceof NetworkNode;
    }

    @Override
    public ArrayList<String> evaluation(Query q) throws Exception
    {
        return this.getOwner().handleRequest(
                owner -> ((NetworkNode)owner).evaluation(q));
    }
}
