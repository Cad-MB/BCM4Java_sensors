package ast.rand;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * This class represents a constant random operand in the abstract syntax tree (AST).
 * It extends the Rand class and implements the eval method to return the constant value.
 */
public class CRand
    implements Rand {

    /**
     * The constant value of the random operand.
     */
    double constant;

    /**
     * Constructor for the CRand class.
     *
     * @param constant The constant value of the random operand.
     */
    public CRand(double constant) {
        this.constant = constant;
    }

    /**
     * Evaluates the constant random operand.
     *
     * @param executionState The current execution state.
     * @return The constant value of the random operand.
     * @throws Exception If an error occurs while evaluating the operand.
     */
    @Override
    public Double eval(ExecutionStateI executionState) throws Exception {
        return constant; // Returns the constant value of the operand
    }

}
