package parser.tree;

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
     * Parses the JSON file into an ArrayList of parser.tree.TreeParsedData.Node objects.
     *
     * @param file the JSON file to parse
     * @return an ArrayList of parser.tree.TreeParsedData.Node objects parsed from the JSON file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static ArrayList<TreeParsedData.Node> parse(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, new TypeReference<ArrayList<TreeParsedData.Node>>() { });
    }

}

