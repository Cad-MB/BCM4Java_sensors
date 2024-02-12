package components;

import ast.query.Query;
import components.client.ClientCI;
import components.node.NodeCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.ArrayList;

public class Connector
    extends AbstractConnector
    implements ClientCI
{
    @Override
    public ArrayList<String> sendRequestB(Query q) throws Exception {
        return ((NodeCI) this.offering).evaluationB(q);
    }
    @Override
    public ArrayList<SensorDataI> sendRequestG(Query q) throws Exception {
        return ((NodeCI) this.offering).evaluationG(q);
    }
}
