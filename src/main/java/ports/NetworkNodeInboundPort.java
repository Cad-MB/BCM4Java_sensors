package ports;

import ast.query.Query;
import components.NetworkNode;
import components.interfaces.NetworkNodeCI;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

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
    public String evaluation(Query q) throws Exception
    {
        return this.getOwner().handleRequest(
                c -> ((NetworkNode)c).evaluation(q));
    }
}
