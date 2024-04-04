package requests;

import ast.query.Query;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

/**
 * This class represents a request in a sensor network, including its URI, query, connection information,
 * and asynchronous flag.
 * It implements the {@link fr.sorbonne_u.cps.sensor_network.interfaces.RequestI} interface.
 */
public class Request
    implements RequestI {

    protected String uri;
    protected QueryI query;
    protected ConnectionInfoI connectionInfo;
    protected boolean async;

    /**
     * Constructs a {@code Request} object with the given URI, query, connection information, and asynchronous flag.
     *
     * @param uri            the URI of the request
     * @param query          the query associated with the request
     * @param connectionInfo the connection information for the client
     * @param async          true if the request is asynchronous, false otherwise
     */
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

    /**
     * This class represents connection information for a request.
     * It implements the {@link fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI} interface.
     */
    public static class ConnectionInfo
        implements ConnectionInfoI {

        String id;
        EndPointDescriptorI endPointInfo;

        /**
         * Constructs a {@code ConnectionInfo} object with the given ID and endpoint descriptor.
         *
         * @param id           the ID of the node
         * @param endPointInfo the endpoint descriptor
         */
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

    @Override
    public String toString() {
        assert query instanceof Query;
        return "Request{" +
               "uri='" + uri + '\'' +
               ", query=" + ((Query) query).queryString() +
               ", connectionInfo=" + connectionInfo +
               ", async=" + async +
               '}';
    }

}
