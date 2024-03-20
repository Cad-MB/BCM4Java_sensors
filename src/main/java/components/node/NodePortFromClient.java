package components.node;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;

import java.util.Arrays;

public class NodePortFromClient
    extends AbstractInboundPort
    implements NodeClientInCI {

    public NodePortFromClient(String uri, ComponentI owner) throws Exception {
        super(uri, NodeClientInCI.class, owner);
        assert owner instanceof Node;
    }

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return this.getOwner().handleRequest(owner -> ((Node) owner).execute(request));
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {
        this.getOwner().runTask(owner -> {
            try {
                ((Node) owner).executeAsync(request);
            } catch (Exception e) {
                System.err.println(Arrays.toString(e.getStackTrace()));
            }
        });
    }

}
