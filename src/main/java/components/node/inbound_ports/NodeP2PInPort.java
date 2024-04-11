package components.node.inbound_ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;

public class NodeP2PInPort
    extends AbstractInboundPort
    implements SensorNodeP2PCI {

    public NodeP2PInPort(String uri, ComponentI owner) throws Exception {
        super(uri, SensorNodeP2PCI.class, owner);
    }

    @Override
    public void ask4Connection(NodeInfoI i) throws Exception {
        this.getOwner().handleRequest(c -> {
            ((SensorNodeP2PImplI) c).ask4Connection(i);
            return null;
        });
    }

    @Override
    public void ask4Disconnection(NodeInfoI i) throws Exception {
        this.getOwner().handleRequest(c -> {
            ((SensorNodeP2PImplI) c).ask4Disconnection(i);
            return null;
        });
    }

    @Override
    public QueryResultI execute(RequestContinuationI reqCont) throws Exception {
        return this.getOwner().handleRequest(c -> ((SensorNodeP2PImplI) c).execute(reqCont));
    }

    @Override
    public void executeAsync(RequestContinuationI reqCont) throws Exception {
        this.getOwner().runTask(owner -> {
            try {
                ((SensorNodeP2PImplI) owner).executeAsync(reqCont);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        });
    }

}
