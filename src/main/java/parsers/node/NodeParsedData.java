package parsers.node;

import sensor_network.Position;

import java.util.ArrayList;

/**
 * This class represents the structure of parsed XML data.
 */
public class NodeParsedData {

    public String id; // The ID of the node
    public Integer range; // The range of the node
    public Position position; // The position of the node
    public ArrayList<SensorParsedData> sensors; // The list of sensors associated with the node
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

    public static class SensorParsedData {

        public String id;
        public float startingValue;
        public float toAdd;

        public SensorParsedData(String id, float startingValue, float toAdd) {
            this.id = id;
            this.startingValue = startingValue;
            this.toAdd = toAdd;
        }

    }
}
