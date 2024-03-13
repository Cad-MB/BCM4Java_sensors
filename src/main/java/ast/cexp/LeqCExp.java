package ast.cexp;

import ast.rand.Rand;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents a "less than or equal" comparison expression in the abstract syntax tree (AST).
 * It extends the CExp class and implements the eval method to evaluate the expression.
 */
public class LeqCExp
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
     * Constructor for the LeqCExp class.
     *
     * @param rand1 The first operand of the comparison expression.
     * @param rand2 The second operand of the comparison expression.
     */
    public LeqCExp(Rand rand1, Rand rand2) {
        this.rand1 = rand1;
        this.rand2 = rand2;
    }

    /**
     * Evaluates the "less than or equal" comparison expression.
     *
     * @param executionState The current execution state.
     * @return The result of evaluating the "less than or equal" comparison expression.
     * @throws Exception If an error occurs while evaluating the operands of the comparison expression.
     */
    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        // Evaluate the two operands of the comparison expression and return the result of the comparison
        Serializable r1 = rand1.eval(executionState);
        Serializable r2 = rand2.eval(executionState);
        assert r1 instanceof Number;
        assert r2 instanceof Number;
        Double num1 = ((Number) r1).doubleValue();
        Double num2 = ((Number) r2).doubleValue();
        return num1.compareTo(num2) <= 0;
    }

    @Override
    public String queryString() {
        return "(" + rand1.queryString() + " <= " + rand2.queryString() + ')';
    }

    @Override
    public String toString() {
        return "LeqCExp{rand1=" + rand1 + ", rand2=" + rand2 + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final LeqCExp exp = (LeqCExp) o;
        return Objects.equals(rand1, exp.rand1) && Objects.equals(rand2, exp.rand2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rand1, rand2);
    }

}
