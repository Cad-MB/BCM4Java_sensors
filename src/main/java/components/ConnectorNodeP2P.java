package components;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;

public class ConnectorNodeP2P
    extends AbstractConnector
    implements SensorNodeP2PCI {

    @Override
    public void ask4Connection(NodeInfoI i) throws Exception {
        ((SensorNodeP2PCI) this.offering).ask4Connection(i);
    }

    @Override
    public void ask4Disconnection(NodeInfoI i) throws Exception {
        ((SensorNodeP2PCI) this.offering).ask4Disconnection(i);

    }

    @Override
    public QueryResultI execute(RequestContinuationI reqCont) throws Exception {
        return ((SensorNodeP2PCI) this.offering).execute(reqCont);
    }

    @Override
    public void executeAsync(RequestContinuationI reqCont) throws Exception {
        ((SensorNodeP2PCI) this.offering).executeAsync(reqCont);
    }

}
