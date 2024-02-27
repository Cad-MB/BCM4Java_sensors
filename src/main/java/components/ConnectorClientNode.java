package components;

import components.client.ClientCI;
import components.node.NodeClientInCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;

public class ConnectorClientNode
    extends AbstractConnector
    implements ClientCI {

    @Override
    public QueryResultI sendRequest(RequestI r) throws Exception {
        return ((NodeClientInCI) this.offering).execute(r);
    }

}
