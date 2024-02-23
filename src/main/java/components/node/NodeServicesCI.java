package components.node;

import ast.query.Query;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.ArrayList;

public interface NodeServicesCI
    extends OfferedCI {

    ArrayList<String> evaluationB(Query q) throws Exception;
    ArrayList<SensorDataI> evaluationG(Query q) throws Exception;
}
