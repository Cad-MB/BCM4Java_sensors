package ast.bexp;

import ast.cexp.CExp;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.Objects;

/**
 * This class represents a boolean expression based on a comparison expression in the abstract syntax tree (AST).
 * It extends the BExp class and implements the eval method to evaluate the expression.
 */
public class CExpBExp
    implements BExp {

    /**
     * The comparison expression to evaluate.
     */
    protected CExp cExp;

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
        return cExp.eval(executionState);
    }

    @Override
    public String queryString() {
        return cExp.queryString();
    }

    @Override
    public String toString() {
        return "CExpBExp{cExp=" + cExp + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CExpBExp exp = (CExpBExp) o;
        return Objects.equals(cExp, exp.cExp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cExp);
    }

}
