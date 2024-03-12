package ast.rand;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.Objects;

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

    @Override
    public String queryString() {
        return Double.toString(constant);
    }

    @Override
    public String toString() {
        return "CRand{constant=" + constant + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CRand rand = (CRand) o;
        return Double.compare(constant, rand.constant) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(constant);
    }

}
