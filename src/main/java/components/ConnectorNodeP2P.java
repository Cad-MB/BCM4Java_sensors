package components;

import components.node.NodeP2PInCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;

public class ConnectorNodeP2P
    extends AbstractConnector
    implements NodeP2PInCI {

    @Override
    public void connect(NodeInfoI neighbour) throws Exception {
        ((NodeP2PInCI) this.offering).connect(neighbour);
    }

    @Override
    public void disconnect(NodeInfoI neighbour) throws Exception {
        ((NodeP2PInCI) this.offering).disconnect(neighbour);
    }

    @Override
    public QueryResultI execute(RequestContinuationI reqCont) throws Exception {
        return ((NodeP2PInCI) this.offering).execute(reqCont);
    }

    @Override
    public void executeAsync(RequestContinuationI reqCont) throws Exception {
        ((NodeP2PInCI) this.offering).executeAsync(reqCont);
    }

    @Override
    public String toString() {
        return "ConnectorNodeP2P{" +
               "offering=" + offering +
               ", requiring=" + requiring +
               '}';
    }

}
