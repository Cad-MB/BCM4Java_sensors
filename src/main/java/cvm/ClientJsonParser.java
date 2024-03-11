package cvm;

import ast.query.Query;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import components.node.Node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class ClientJsonParser {


    public static ArrayList<ClientParsedData.Client> parse(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, new TypeReference<ArrayList<ClientParsedData.Client>>() { });
    }

}

class ClientParsedData {

    public static class Client {

        public String id; // The ID of the client
        public ArrayList<String> queries; // The list of queries
        public ArrayList<String> target_nodes_ids; // The list of initial node to target
        public Integer frequency; // The frequency between each request

        @Override
        public String toString() {
            return "Client{" +
                   "id='" + id + '\'' +
                   ", queries=" + queries +
                   ", nodes=" + target_nodes_ids +
                   ", frequency=" + frequency +
                   '}';
        }

    }
}
