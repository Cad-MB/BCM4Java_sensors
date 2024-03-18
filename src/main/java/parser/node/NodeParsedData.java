package parser.node;

import java.util.ArrayList;

/**
 * This class represents the structure of parsed JSON data.
 */
public class NodeParsedData {

    /**
     * Represents a node parsed from JSON data.
     */
    public static class Node {

        public String id; // The ID of the node
        public Integer range; // The range of the node
        public Position position; // The position of the node
        public ArrayList<Sensor> sensors; // The list of sensors associated with the node
        public int delay;

        @Override
        public String toString() {
            return "Node{" +
                   "id='" + id + '\'' +
                   ", position=" + position +
                   ", sensors=" + sensors +
                   ", delay=" + delay +
                   '}';
        }

    }

    /**
     * Represents a sensor parsed from JSON data.
     */
    public static class Sensor {

        public String id; // The ID of the sensor
        public Float value; // The value of the sensor

        @Override
        public String toString() {
            return "{" +
                   "id='" + id + '\'' +
                   ", value=" + value +
                   '}';
        }

    }

    /**
     * Represents a position parsed from JSON data.
     */
    public static class Position {

        public Float x, y; // The coordinates of the position

        @Override
        public String toString() {
            return "{" +
                   "x=" + x +
                   ", y=" + y +
                   '}';
        }

    }

}
