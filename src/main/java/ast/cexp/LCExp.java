package ast.cexp;

import ast.rand.Rand;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.Objects;

/**
 * This class represents a "less than" comparison expression in the abstract syntax tree (AST).
 * It extends the CExp class and implements the eval method to evaluate the expression.
 */
public class LCExp
    implements CExp {

    /**
     * The first operand of the comparison expression.
     */
    protected Rand rand1;
    /**
     * The second operand of the comparison expression.
     */
    protected Rand rand2;

    /**
     * Constructor for the LCExp class.
     *
     * @param rand1 The first operand of the comparison expression.
     * @param rand2 The second operand of the comparison expression.
     */
    public LCExp(Rand rand1, Rand rand2) {
        this.rand1 = rand1;
        this.rand2 = rand2;
    }

    /**
     * Evaluates the "less than" comparison expression.
     *
     * @param executionState The current execution state.
     * @return The result of evaluating the "less than" comparison expression.
     * @throws Exception If an error occurs while evaluating the operands of the comparison expression.
     */
    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        double r1 = CExp.getDoubleOfNumber(rand1.eval(executionState));
        double r2 = CExp.getDoubleOfNumber(rand2.eval(executionState));
        return r1 < r2;
    }

    @Override
    public String queryString() {
        return "(" + rand1.queryString() + " < " + rand2.queryString() + ')';
    }

    @Override
    public String toString() {
        return "LCExp{rand1=" + rand1 + ", rand2=" + rand2 + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final LCExp exp = (LCExp) o;
        return Objects.equals(rand1, exp.rand1) && Objects.equals(rand2, exp.rand2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rand1, rand2);
    }

}
