package ast.base;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * This class represents a base with a dynamic position determined by the execution in the abstract syntax tree (AST) of queries.
 * It extends the Base class and implements the eval method to return the position of the base during execution.
 */
public class RBase
    implements Base {

    /**
     * Constructor for the RBase class.
     */
    public RBase() {
    }

    /**
     * Returns the position of the base during execution.
     *
     * @param executionState The current execution state.
     * @return The position of the base during execution.
     */
    @Override
    public PositionI eval(ExecutionStateI executionState) {
        return executionState.getProcessingNode().getPosition();
    }

}
