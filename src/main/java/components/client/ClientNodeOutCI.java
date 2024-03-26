package components.client;

import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;

public interface ClientNodeOutCI
    extends RequiredCI {

    QueryResultI sendRequest(RequestI r) throws Exception;
    void sendAsyncRequest(RequestI req) throws Exception;
}