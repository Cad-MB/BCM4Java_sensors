package components;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;

public class ConnectorNodeClient
    extends AbstractConnector
    implements RequestResultCI {

    @Override
    public void acceptRequestResult(String s, QueryResultI i) throws Exception {
        ((RequestResultCI) this.offering).acceptRequestResult(s, i);
    }

}
