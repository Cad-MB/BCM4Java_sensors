package components.node.inbound_ports;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingImplI;

public class NodeRequestingInPort
    extends AbstractInboundPort
    implements RequestingCI {

    public NodeRequestingInPort(String uri, ComponentI owner, String pluginURI) throws Exception {
        super(uri, RequestingCI.class, owner, pluginURI, null);
    }

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return this.getOwner().handleRequest(new AbstractComponent.AbstractService<QueryResultI>(this.pluginURI) {
            @Override
            public QueryResultI call() throws Exception {
                return ((RequestingImplI) this.getServiceProviderReference()).execute(request);
            }
        });
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {
        this.getOwner().handleRequest(new AbstractComponent.AbstractService<Void>(this.pluginURI) {
            @Override
            public Void call() throws Exception {
                ((RequestingImplI) this.getServiceProviderReference()).executeAsync(request);
                return null;
            }
        });
    }

}
