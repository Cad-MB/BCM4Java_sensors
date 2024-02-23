package components;

import ast.query.Query;
import components.client.ClientCI;
import components.node.NodeServicesCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.ArrayList;

public class ConnectorClientNode
    extends AbstractConnector
    implements ClientCI {
    @Override
    public ArrayList<String> sendRequestB(Query q) throws Exception {
        return ((NodeServicesCI) this.offering).evaluationB(q);
    }

    @Override
    public ArrayList<SensorDataI> sendRequestG(Query q) throws Exception {
        return ((NodeServicesCI) this.offering).evaluationG(q);
    }
}
