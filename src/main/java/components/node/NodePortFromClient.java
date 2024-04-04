package components.node;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingImplI;

public class NodePortFromClient
    extends AbstractInboundPort
    implements RequestingCI {

    public NodePortFromClient(String uri, ComponentI owner) throws Exception {
        super(uri, RequestingCI.class, owner);
    }

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return this.getOwner().handleRequest(owner -> ((RequestingImplI) owner).execute(request));
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {
        this.getOwner().runTask(owner -> {
            try {
                ((RequestingImplI) owner).executeAsync(request);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        });
    }

}
