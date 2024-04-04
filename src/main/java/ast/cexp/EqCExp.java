package ast.cexp;

import ast.rand.Rand;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.Objects;

/**
 * This class represents an equality comparison expression in the abstract syntax tree (AST).
 * It extends the CExp class and implements the eval method to evaluate the expression.
 */
public class EqCExp
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
        return rand1.eval(executionState).equals(rand2.eval(executionState));
    }

    @Override
    public String queryString() {
        return "(" + rand1.queryString() + " = " + rand2.queryString() + ')';
    }

    @Override
    public String toString() {
        return "EqCExp{rand1=" + rand1 + ", rand2=" + rand2 + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final EqCExp exp = (EqCExp) o;
        return Objects.equals(rand1, exp.rand1) && Objects.equals(rand2, exp.rand2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rand1, rand2);
    }

}
