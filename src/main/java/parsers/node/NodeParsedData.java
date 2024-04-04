package parsers.node;

import javafx.util.Pair;
import sensor_network.Position;

import java.util.ArrayList;

/**
 * This class represents the structure of parsed XML data.
 */
public class NodeParsedData {


    public String id; // The ID of the node
    public Integer range; // The range of the node
    public Position position; // The position of the node
    public ArrayList<Pair<String, Float>> sensors; // The list of sensors associated with the node
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
