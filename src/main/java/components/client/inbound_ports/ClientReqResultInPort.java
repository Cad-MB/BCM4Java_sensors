package components.client.inbound_ports;

import components.client.ClientPlugin;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;

public class ClientReqResultInPort
    extends AbstractInboundPort
    implements RequestResultCI {

    public ClientReqResultInPort(String uri, ComponentI owner, String pluginUri) throws Exception {
        super(uri, RequestResultCI.class, owner, pluginUri, null);
    }

    @Override
    public void acceptRequestResult(String requestUri, QueryResultI res) throws Exception {
        this.getOwner().runTask(new AbstractComponent.AbstractTask(this.getPluginURI()) {
            @Override
            public void run() {
                ((ClientPlugin) this.getTaskProviderReference()).acceptQueryResult(requestUri, res);
            }
        });
    }

}