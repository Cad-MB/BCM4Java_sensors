package ast.cexp;

import ast.rand.Rand;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * This class represents an equality comparison expression in the abstract syntax tree (AST).
 * It extends the CExp class and implements the eval method to evaluate the expression.
 */
public class EqCExp
    implements CExp {

    /**
     * The first operand of the comparison expression.
     */
    Rand rand1;
    /**
     * The second operand of the comparison expression.
     */
    Rand rand2;

    /**
     * Constructor for the EqCExp class.
     *
     * @param rand1 The first operand of the comparison expression.
     * @param rand2 The second operand of the comparison expression.
     */
    public EqCExp(Rand rand1, Rand rand2) {
        this.rand1 = rand1;
        this.rand2 = rand2;
    }

    /**
     * Evaluates the equality comparison expression.
     *
     * @param executionState The current execution state.
     * @return The result of evaluating the equality comparison expression.
     * @throws Exception If an error occurs while evaluating the operands of the comparison expression.
     */
    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        // Evaluate the two operands of the comparison expression and return the result of their equality
        return rand1.eval(executionState).equals(rand2.eval(executionState));
    }
}
