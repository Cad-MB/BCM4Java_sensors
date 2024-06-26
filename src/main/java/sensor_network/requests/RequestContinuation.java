package sensor_network.requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * This class represents a continuation of a request in a sensor network, including its execution state.
 * It extends the {@link Request} class and implements the {@link fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI} interface.
 */
public class RequestContinuation
    extends Request
    implements RequestContinuationI {

    protected final ExecutionStateI executionState;

    /**
     * Constructs a {@code RequestContinuation} object with the given request and execution state.
     *
     * @param request        the original request
     * @param executionState the execution state of the request
     */
    public RequestContinuation(RequestI request, ExecutionStateI executionState) {
        super(request.requestURI(), request.getQueryCode(), request.clientConnectionInfo(), request.isAsynchronous());
        this.executionState = executionState;
    }

    /**
     * Gets the execution state of the request continuation.
     *
     * @return the execution state of the request continuation
     */
    @Override
    public ExecutionStateI getExecutionState() {
        return executionState;
    }

    /**
     * Returns a string representation of the request continuation.
     *
     * @return a string representation of the request continuation
     */
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
