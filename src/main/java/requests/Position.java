package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

import java.awt.*;

public class Position
    implements PositionI {

    private final double x, y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double distance(PositionI p) {
        assert p instanceof Position;

        double x2 = ((Position) p).getX();
        double y2 = ((Position) p).getY();

        return ((int) Point.distance(x, y, x2, y2));
    }

    @Override
    public Direction directionFrom(PositionI p) {
        if (this.northOf(p) && this.eastOf(p)) {
            return Direction.NE;
        } else if (this.northOf(p) && this.westOf(p)) {
            return Direction.NW;
        } else if (this.southOf(p) && this.eastOf(p)) {
            return Direction.SE;
        } else if (this.southOf(p) && this.westOf(p)) {
            return Direction.SW;
        }
        return null;
    }

    @Override
    public boolean northOf(PositionI p) {
        assert p instanceof Position;
        double y2 = ((Position) p).getY();
        return y - y2 > 0;
    }

    @Override
    public boolean southOf(PositionI p) {
        assert p instanceof Position;
        double y2 = ((Position) p).getY();
        return y - y2 < 0;
    }

    @Override
    public boolean eastOf(PositionI p) {
        assert p instanceof Position;
        double x2 = ((Position) p).getX();
        return x - x2 > 0;
    }

    @Override
    public boolean westOf(PositionI p) {
        assert p instanceof Position;
        double x2 = ((Position) p).getX();
        return x - x2 < 0;
    }

    public double getX() {
        return x;
    }

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
