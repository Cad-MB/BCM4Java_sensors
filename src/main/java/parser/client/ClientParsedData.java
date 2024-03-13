package parser.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ClientParsedData {

    public static class Client {

        @JsonProperty("id")
        public String id; // The ID of the client

        @JsonProperty("queries")
        public ArrayList<String> queries; // The list of queries

        @JsonProperty("target_node_ids")
        public ArrayList<String> targetNodesIds; // The list of initial node to target

        @JsonProperty("frequency")
        public Integer frequency; // The frequency between each request

        @Override
        public String toString() {
            return "Client{" +
                   "id='" + id + '\'' +
                   ", queries=" + queries +
                   ", nodes=" + targetNodesIds +
                   ", frequency=" + frequency +
                   '}';
        }

    }

}
