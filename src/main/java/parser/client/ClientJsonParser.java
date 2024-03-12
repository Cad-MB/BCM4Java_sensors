package parser.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class ClientJsonParser {

    public static ArrayList<ClientParsedData.Client> parse(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, new TypeReference<ArrayList<ClientParsedData.Client>>() { });
    }

}

