package components.client;

import ast.query.Query;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.ArrayList;

public interface ClientCI
    extends RequiredCI {

    QueryResultI sendRequest(RequestI r) throws Exception;

}
