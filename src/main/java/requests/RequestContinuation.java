package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

public class RequestContinuation
    extends Request
    implements RequestContinuationI
{

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

}
