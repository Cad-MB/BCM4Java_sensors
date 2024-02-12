package connectors;

import ast.query.Query;
import components.interfaces.ClientCI;
import components.interfaces.NetworkNodeCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import sensor.SensorData;

import java.util.ArrayList;

public class Connector
    extends AbstractConnector
    implements ClientCI
{
    @Override
    public ArrayList<String> sendRequestB(Query q) throws Exception {
        return ((NetworkNodeCI)this.offering).evaluationB(q);
    }
    @Override
    public ArrayList<SensorDataI> sendRequestG(Query q) throws Exception {
        return ((NetworkNodeCI)this.offering).evaluationG(q);
    }
}
