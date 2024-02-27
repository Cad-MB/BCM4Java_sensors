package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class RequestContinuation
    extends Request
    implements RequestContinuationI {

    private final ExecutionStateI executionState;

    public RequestContinuation(
        RequestI request, ExecutionStateI executionState
    ) {
        super(request.requestURI(), request.getQueryCode(), request.clientConnectionInfo(), request.isAsynchronous());
        this.executionState = executionState;
    }

    @Override
    public ExecutionStateI getExecutionState() {
        return executionState;
    }

    @Override
    public String toString() {
        return "RequestContinuation{" +
               "executionState=" + executionState +
               ", uri='" + uri + '\'' +
               ", query=" + query +
               ", connectionInfo=" + connectionInfo +
               ", async=" + async +
               '}';
    }

}
