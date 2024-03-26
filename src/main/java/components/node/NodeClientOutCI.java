package components.node;

import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;

public interface NodeClientOutCI
    extends RequiredCI {

    void sendQueryResult(String requestId, QueryResultI result) throws Exception;

}
