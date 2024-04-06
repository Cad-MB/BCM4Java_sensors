package parsers.client;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;

public class ClientXMLParser {

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    public static ArrayList<ClientParsedData> parse(File file) throws Exception {
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(file);
        NodeList clients = doc.getDocumentElement().getElementsByTagName("client");
        ArrayList<ClientParsedData> clientList = new ArrayList<>();

        for (int i = 0; i < clients.getLength(); i++) {
            Node client = clients.item(i);
            if (client.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element clientEl = (Element) client;
            Node id = clientEl.getElementsByTagName("id").item(0);
            Node frequency = clientEl.getElementsByTagName("frequency").item(0);
            Node queries = clientEl.getElementsByTagName("queries").item(0);
            Node targetNodes = clientEl.getElementsByTagName("targetNodes").item(0);

            ClientParsedData parsedClient = new ClientParsedData();
            parsedClient.id = id.getTextContent();
            parsedClient.frequency = Integer.parseInt(frequency.getTextContent().trim());
            parsedClient.queries = parseList(queries);
            parsedClient.targetNodesIds = parseList(targetNodes);

            clientList.add(parsedClient);
        }
        return clientList;
    }

    private static ArrayList<String> parseList(Node queries) {
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < queries.getChildNodes().getLength(); i++) {
            Node nodeId = queries.getChildNodes().item(i);
            if (nodeId.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            strings.add(nodeId.getTextContent().trim());
        }
        return strings;
    }

}

