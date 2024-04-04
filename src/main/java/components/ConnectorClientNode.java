package components;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;

public class ConnectorClientNode
    extends AbstractConnector
    implements RequestingCI {

    @Override
    public QueryResultI execute(RequestI i) throws Exception {
        return ((RequestingCI) this.offering).execute(i);
    }

    @Override
    public void executeAsync(RequestI i) throws Exception {
        ((RequestingCI) this.offering).executeAsync(i);
    }

}
