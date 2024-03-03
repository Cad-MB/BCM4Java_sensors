package ast.base;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * This class represents a base with a specific position in the abstract syntax tree (AST) of queries.
 * It extends the Base class and implements the eval method to return the position of the base.
 */
public class ABase
    implements Base {

    /**
     * The position of the base.
     */
    PositionI position;

    /**
     * Constructor for the ABase class.
     *
     * @param position The position of the base.
     */
    public ABase(PositionI position) {
        this.position = position;
    }

    /**
     * Returns the position of the base.
     *
     * @param executionState The current execution state.
     * @return The position of the base.
     */
    @Override
    public PositionI eval(ExecutionStateI executionState) {
        return position;
    }

}
