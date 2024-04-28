package components.client;

import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;

/**
 * The ClientNodeOutCI interface represents the required interface that a client node
 * component must implement to be able to send requests and receive query results.
 */
public interface ClientCI
    extends RequiredCI {

    /**
     * Sends a request and returns the query result.
     *
     * @param r the request to send
     * @return the query result
     * @throws Exception if an error occurs during request sending
     */
    QueryResultI sendRequest(RequestI r) throws Exception;


    /**
     * Sends an asynchronous request to a client node component. The method takes a {@code RequestI} object as a parameter
     * and sends it to the client node for processing. The method does not block and returns immediately. If an error occurs
     * during the request sending process, an exception is thrown.
     *
     * @param req the request to send to the node
     * @throws Exception if an error occurs during the request sending process
     */
    void sendAsyncRequest(RequestI req) throws Exception;

}
