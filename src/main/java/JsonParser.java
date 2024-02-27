import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class JsonParser {

    public static ArrayList<ParsedData.Node> parse(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, new TypeReference<ArrayList<ParsedData.Node>>() { });
    }

}

class ParsedData {

    public static class Node {

        public String id;
        public Integer range;
        public Position position;
        public ArrayList<Sensor> sensors;

        @Override
        public String toString() {
            return "Node{" +
                   "id='" + id + '\'' +
                   ", position=" + position +
                   ", sensors=" + sensors +
                   '}';
        }

    }

    public static class Sensor {

        public String id;
        public Float value;

        @Override
        public String toString() {
            return "{" +
                   "id='" + id + '\'' +
                   ", value=" + value +
                   '}';
        }

    }

    public static class Position {

        public Float x, y;

        @Override
        public String toString() {
            return "{" +
                   "x=" + x +
                   ", y=" + y +
                   '}';
        }

    }

}
