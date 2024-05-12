package ast.base;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import sensor_network.Position;

import java.util.Objects;

/**
 * This class represents a base with a specific position in the abstract syntax tree (AST) of queries.
 * It extends the Base class and implements the eval method to return the position of the base.
 */
public class ABase
    implements Base {

    /**
     * The position of the base.
     */
    protected PositionI position;

    /**
     * Constructor for the ABase class.
     *
     * @param position The position of the base.
     */
    public ABase(PositionI position) {
        this.position = position;
    }

    /**
     * Returns the position of the base.
     *
     * @param executionState The current execution state.
     * @return The position of the base.
     */
    @Override
    public PositionI eval(ExecutionStateI executionState) {
        return position;
    }

    /**
     * Constructs a string representation of the position.
     *
     * @return A string representation of the position in the format (x, y).
     */
    @Override
    public String queryString() {
        Position p = (Position) position;
        return "(" + p.getX() + ", " + p.getY() + ')';
    }

    /**
     * Converts the ABase instance to a string format.
     *
     * @return A string representation of the ABase instance.
     */
    @Override
    public String toString() {
        return "ABase{position=" + position + '}';
    }

    /**
     * Checks if this ABase is equal to another object.
     *
     * @param o The object to compare with this instance.
     * @return true if the given object is also an ABase and has the same position.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ABase base = (ABase) o;
        return Objects.equals(position, base.position);
    }

    /**
     * Computes the hash code for the ABase instance.
     *
     * @return The hash code of this ABase instance.
     */
    @Override
    public int hashCode() {
        return Objects.hash(position);
    }

}
