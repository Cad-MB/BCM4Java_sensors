package sensor_network;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

import java.awt.*;

/**
 * This class represents a position in a two-dimensional space.
 * It implements the {@link fr.sorbonne_u.cps.sensor_network.interfaces.PositionI} interface.
 */
public class Position
    implements PositionI {

    protected final double x, y;

    /**
     * Constructs a {@code Position} object with the given x and y coordinates.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Computes the Euclidean distance between this position and another position.
     *
     * @param p the other position
     * @return the Euclidean distance between this position and the other position
     */
    @Override
    public double distance(PositionI p) {
        assert p instanceof Position;

        double x2 = ((Position) p).getX();
        double y2 = ((Position) p).getY();

        return ((int) Point.distance(x, y, x2, y2));
    }

    /**
     * Determines the direction from this position to another position.
     *
     * @param p the other position
     * @return the direction from this position to the other position
     */
    @Override
    public Direction directionFrom(PositionI p) {
        if (this.northOf(p)) {
            return this.eastOf(p)
                ? Direction.NE
                // 90
                : Direction.NW;
        } else if (this.southOf(p)) {
            return this.eastOf(p)
                ? Direction.SE
                : Direction.SW;
        } else {
            throw new RuntimeException("aucune direction");
        }
    }

    /**
     * Checks if this position is north of another position.
     *
     * @param p the other position
     * @return true if this position is north of the other position, false otherwise
     */
    @Override
    public boolean northOf(PositionI p) {
        assert p instanceof Position;
        double y2 = ((Position) p).getY();
        return y2 >= y;
    }

    /**
     * Checks if this position is south of another position.
     *
     * @param p the other position
     * @return true if this position is south of the other position, false otherwise
     */
    @Override
    public boolean southOf(PositionI p) {
        assert p instanceof Position;
        double y2 = ((Position) p).getY();
        return y2 < y;
    }

    /**
     * Checks if this position is east of another position.
     *
     * @param p the other position
     * @return true if this position is east of the other position, false otherwise
     */
    @Override
    public boolean eastOf(PositionI p) {
        assert p instanceof Position;
        double x2 = ((Position) p).getX();
        return x2 >= x;
    }

    /**
     * Checks if this position is west of another position.
     *
     * @param p the other position
     * @return true if this position is west of the other position, false otherwise
     */
    @Override
    public boolean westOf(PositionI p) {
        assert p instanceof Position;
        double x2 = ((Position) p).getX();
        return x2 < x;
    }

    /**
     * Gets the x-coordinate of this position.
     *
     * @return the x-coordinate of this position
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of this position.
     *
     * @return the y-coordinate of this position
     */
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Position{" +
               "x=" + x +
               ", y=" + y +
               '}';
    }

}
