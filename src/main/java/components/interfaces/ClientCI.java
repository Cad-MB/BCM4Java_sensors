package components.interfaces;

import ast.query.Query;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.ArrayList;

public interface ClientCI
extends RequiredCI
{
    ArrayList<String> sendRequestB(Query q) throws Exception;
    ArrayList<SensorDataI> sendRequestG(Query q) throws Exception;
}
