package components.node;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;

public class NodePortFromP2P
    extends AbstractInboundPort
    implements NodeP2PInCI {

    public NodePortFromP2P(String uri, ComponentI owner) throws Exception {
        super(uri, NodeP2PInCI.class, owner);
        assert owner instanceof Node;
    }

    @Override
    public void connect(NodeInfoI neighbour) throws Exception {
        this.getOwner().handleRequest(c -> {
            ((Node) c).connect(neighbour);
            return null;
        });
    }

    @Override
    public void disconnect(NodeInfoI neighbour) throws Exception {
        this.getOwner().handleRequest(c -> {
            ((Node) c).disconnect(neighbour);
            return null;
        });
    }

    @Override
    public QueryResultI execute(RequestContinuationI reqCont) throws Exception {
        return this.getOwner().handleRequest(c -> ((Node) c).execute(reqCont));
    }

    @Override
    public void executeAsync(RequestContinuationI reqCont) throws Exception {
        this.getOwner().runTask(owner -> {
            try {
                ((Node) owner).executeAsync(reqCont);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public String toString() {
        return "NodePortFromP2P{" +
               "owner=" + owner +
               '}';
    }

}
