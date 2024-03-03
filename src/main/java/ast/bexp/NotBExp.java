package ast.bexp;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * This class represents a "NOT" boolean expression in the abstract syntax tree (AST).
 * It extends the BExp class and implements the eval method to evaluate the expression.
 */
public class NotBExp
    implements BExp {

    /**
     * The boolean expression to negate.
     */
    BExp bExp;

    /**
     * Constructs a NotBExp object.
     *
     * @param bExp The boolean expression to negate.
     */
    public NotBExp(BExp bExp) {
        this.bExp = bExp;
    }

    /**
     * Evaluates the "NOT" boolean expression.
     *
     * @param executionState The current execution state.
     * @return The result of evaluating the negated boolean expression.
     * @throws Exception If an error occurs during the evaluation of the boolean expression.
     */
    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        // Negation of the result of evaluating the boolean expression
        return !bExp.eval(executionState);
    }

}
