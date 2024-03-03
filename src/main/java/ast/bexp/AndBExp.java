package ast.bexp;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * This class represents an "AND" boolean expression in the abstract syntax tree (AST).
 * It extends the BExp class and implements the eval method to evaluate the expression.
 */
public class AndBExp
    implements BExp {

    /**
     * The first boolean expression to evaluate.
     */
    BExp bExp1;
    /**
     * The second boolean expression to evaluate.
     */
    BExp bExp2;

    /**
     * Constructs an AndBExp object.
     *
     * @param bExp1 The first boolean expression to evaluate.
     * @param bExp2 The second boolean expression to evaluate.
     */
    public AndBExp(BExp bExp1, BExp bExp2) {
        this.bExp1 = bExp1;
        this.bExp2 = bExp2;
    }

    /**
     * Evaluates the "AND" boolean expression.
     *
     * @param executionState The current execution state.
     * @return The result of evaluating the "AND" boolean expression.
     * @throws Exception If an error occurs during the evaluation of boolean expressions.
     */
    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        // Evaluates both boolean expressions and returns their logical conjunction
        return bExp1.eval(executionState) && bExp2.eval(executionState);
    }

}
