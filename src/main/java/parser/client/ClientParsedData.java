package parser.client;

import java.util.ArrayList;

public class ClientParsedData {

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
