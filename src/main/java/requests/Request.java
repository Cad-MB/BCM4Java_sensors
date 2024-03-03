package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

public class Request
    implements RequestI {

    String uri;
    QueryI query;
    ConnectionInfoI connectionInfo;
    boolean async;

    public Request(String uri, QueryI query, ConnectionInfoI connectionInfo, boolean async) {
        this.uri = uri;
        this.query = query;
        this.connectionInfo = connectionInfo;
        this.async = async;
    }

    /**
     * Gets the URI of the request.
     *
     * @return the URI of the request
     */
    @Override
    public String requestURI() {
        return uri;
    }

    /**
     * Gets the query associated with the request.
     *
     * @return the query associated with the request
     */
    @Override
    public QueryI getQueryCode() {
        return query;
    }

    /**
     * Checks if the request is asynchronous.
     *
     * @return true if the request is asynchronous, false otherwise
     */
    @Override
    public boolean isAsynchronous() {
        return async;
    }

    /**
     * Gets the connection information for the client.
     *
     * @return the connection information for the client
     */
    @Override
    public ConnectionInfoI clientConnectionInfo() {
        return connectionInfo;
    }

    public static class ConnectionInfo
        implements ConnectionInfoI {

        String id;
        EndPointDescriptorI endPointInfo;

        public ConnectionInfo(String id, EndPointDescriptorI endPointInfo) {
            this.id = id;
            this.endPointInfo = endPointInfo;
        }

        /**
         * Gets the ID of the node.
         *
         * @return the ID of the node
         */
        @Override
        public String nodeIdentifier() {
            return id;
        }

        /**
         * Gets the endpoint descriptor.
         *
         * @return the endpoint descriptor
         */
        @Override
        public EndPointDescriptorI endPointInfo() {
            return endPointInfo;
        }

    }

}
