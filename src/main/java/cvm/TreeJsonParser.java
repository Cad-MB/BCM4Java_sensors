package cvm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class provides methods to parse JSON files into Java objects using Jackson library.
 */
public class TreeJsonParser {

    /**
     * Parses the JSON file into an ArrayList of cvm.TreeParsedData.Node objects.
     *
     * @param file the JSON file to parse
     * @return an ArrayList of cvm.TreeParsedData.Node objects parsed from the JSON file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static ArrayList<TreeParsedData.Node> parse(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, new TypeReference<ArrayList<TreeParsedData.Node>>() { });
    }

}

/**
 * This class represents the structure of parsed JSON data.
 */
class TreeParsedData {

    /**
     * Represents a node parsed from JSON data.
     */
    public static class Node {

        public String id; // The ID of the node
        public Integer range; // The range of the node
        public Position position; // The position of the node
        public ArrayList<Sensor> sensors; // The list of sensors associated with the node

        @Override
        public String toString() {
            return "Node{" +
                   "id='" + id + '\'' +
                   ", position=" + position +
                   ", sensors=" + sensors +
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
