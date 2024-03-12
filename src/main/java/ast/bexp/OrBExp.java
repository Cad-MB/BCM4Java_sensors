package ast.bexp;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.Objects;

/**
 * This class represents an "OR" boolean expression in the abstract syntax tree (AST).
 * It extends the BExp class and implements the eval method to evaluate the expression.
 */
public class OrBExp
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
     * Constructs an OrBExp object.
     *
     * @param bExp1 The first boolean expression to evaluate.
     * @param bExp2 The second boolean expression to evaluate.
     */
    public OrBExp(BExp bExp1, BExp bExp2) {
        this.bExp1 = bExp1;
        this.bExp2 = bExp2;
    }

    /**
     * Evaluates the "OR" boolean expression.
     *
     * @param executionState The current execution state.
     * @return The result of evaluating the "OR" boolean expression.
     * @throws Exception If an error occurs during the evaluation of the boolean expressions.
     */
    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        // Evaluates both boolean expressions and returns their logical disjunction
        return bExp1.eval(executionState) || bExp2.eval(executionState);
    }

    @Override
    public String queryString() {
        return "(" + bExp1.queryString() + " or " + bExp2.queryString() + ')';
    }

    @Override
    public String toString() {
        return "OrBExp{bExp1=" + bExp1 + ", bExp2=" + bExp2 + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final OrBExp exp = (OrBExp) o;
        return Objects.equals(bExp1, exp.bExp1) && Objects.equals(bExp2, exp.bExp2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bExp1, bExp2);
    }

}
