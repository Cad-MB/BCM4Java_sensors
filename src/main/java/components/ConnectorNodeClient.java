package components;

import components.client.ClientPortFromNode;
import components.node.NodeClientOutCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;

public class ConnectorNodeClient
    extends AbstractConnector
    implements NodeClientOutCI {

    @Override
    public void sendQueryResult(String requestURI, QueryResultI result) throws Exception {
        ((ClientPortFromNode) this.offering).acceptRequestResult(requestURI, result);
    }

}
