package ast.bexp;

import ast.cexp.CExp;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * This class represents a boolean expression based on a comparison expression in the abstract syntax tree (AST).
 * It extends the BExp class and implements the eval method to evaluate the expression.
 */
public class CExpBExp
    implements BExp {

    /**
     * The comparison expression to evaluate.
     */
    CExp cExp;

    /**
     * Constructs a CExpBExp object.
     *
     * @param cExp The comparison expression to evaluate.
     */
    public CExpBExp(CExp cExp) {
        this.cExp = cExp;
    }

    /**
     * Evaluates the comparison expression to obtain the result of the boolean expression.
     *
     * @param executionState The current execution state.
     * @return The result of evaluating the comparison expression, representing the result of the boolean expression.
     * @throws Exception If an error occurs during the evaluation of the comparison expression.
     */
    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        return cExp.eval(executionState); // Evaluates the comparison expression and returns its result
    }

}
