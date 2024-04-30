package components.node.inbound_ports;

import components.node.NodePlugin;
import fr.sorbonne_u.components.AbstractComponent;
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

    public NodeP2PInPort(String uri, ComponentI owner, String pluginURI) throws Exception {
        super(uri, SensorNodeP2PCI.class, owner, pluginURI, null);
    }

    @Override
    public void ask4Connection(NodeInfoI neighbour) throws Exception {
        this.getOwner().runTask(new AbstractComponent.AbstractTask(this.getPluginURI()) {
            @Override
            public void run() {
                try {
                    ((NodePlugin) this.getTaskProviderReference()).ask4Connection(neighbour);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void ask4Disconnection(NodeInfoI i) throws Exception {
        this.getOwner().runTask(new AbstractComponent.AbstractTask(this.getPluginURI()) {
            @Override
            public void run() {
                try {
                    ((SensorNodeP2PImplI) this.getTaskProviderReference()).ask4Disconnection(i);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public QueryResultI execute(RequestContinuationI reqCont) throws Exception {
        return this.getOwner().handleRequest(new AbstractComponent.AbstractService<QueryResultI>(this.getPluginURI()) {
            @Override
            public QueryResultI call() throws Exception {
                return ((SensorNodeP2PImplI) this.getServiceProviderReference()).execute(reqCont);
            }
        });
    }

    @Override
    public void executeAsync(RequestContinuationI reqCont) throws Exception {
        this.getOwner().runTask(new AbstractComponent.AbstractTask(this.getPluginURI()) {
            @Override
            public void run() {
                try {
                    ((SensorNodeP2PImplI) this.getTaskProviderReference()).executeAsync(reqCont);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

}
