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

    public Request(final String uri, final QueryI query, final ConnectionInfoI connectionInfo, final boolean async) {
        this.uri = uri;
        this.query = query;
        this.connectionInfo = connectionInfo;
        this.async = async;
    }

    @Override
    public String requestURI() {
        return uri;
    }

    @Override
    public QueryI getQueryCode() {
        return query;
    }

    @Override
    public boolean isAsynchronous() {
        return async;
    }

    @Override
    public ConnectionInfoI clientConnectionInfo() {
        return connectionInfo;
    }

    public static class ConnectionInfo
        implements ConnectionInfoI {

        String id;
        EndPointDescriptorI endPointInfo;

        public ConnectionInfo(final String id, final EndPointDescriptorI endPointInfo) {
            this.id = id;
            this.endPointInfo = endPointInfo;
        }


        @Override
        public String nodeIdentifier() {
            return id;
        }

        @Override
        public EndPointDescriptorI endPointInfo() {
            return endPointInfo;
        }

    }

}
