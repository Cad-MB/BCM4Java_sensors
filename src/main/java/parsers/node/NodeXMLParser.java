package parsers.node;

import javafx.util.Pair;
import org.w3c.dom.*;
import sensor_network.Position;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;

public class NodeXMLParser {

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    public static ArrayList<NodeParsedData> parse(File file) throws Exception {
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(file);
        NodeList foret = doc.getDocumentElement().getElementsByTagName("node");
        ArrayList<NodeParsedData> nodeList = new ArrayList<>();

        for (int i = 0; i < foret.getLength(); i++) {
            Node node = foret.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element nodeEl = (Element) node;
            Node id = nodeEl.getElementsByTagName("id").item(0);
            Node range = nodeEl.getElementsByTagName("range").item(0);
            Node delay = nodeEl.getElementsByTagName("delay").item(0);
            Node position = nodeEl.getElementsByTagName("position").item(0);
            Node sensors = nodeEl.getElementsByTagName("sensors").item(0);

            NamedNodeMap attrs = position.getAttributes();
            float posX = Float.parseFloat(attrs.getNamedItem("x").getTextContent().trim());
            float posY = Float.parseFloat(attrs.getNamedItem("y").getTextContent().trim());
            Position pos = new Position(posX, posY);

            NodeParsedData parsedNode = new NodeParsedData();
            parsedNode.id = id.getTextContent();
            parsedNode.range = Integer.parseInt(range.getTextContent());
            parsedNode.delay = Integer.parseInt(delay.getTextContent());
            parsedNode.position = pos;
            parsedNode.sensors = parseSensors(sensors);

            nodeList.add(parsedNode);
        }
        return nodeList;
    }

    private static ArrayList<Pair<String, Float>> parseSensors(Node sensorsEl) {
        ArrayList<Pair<String, Float>> ret = new ArrayList<>();
        NodeList sensors = sensorsEl.getChildNodes();
        for (int i = 0; i < sensors.getLength(); i++) {
            Node sensor = sensors.item(i);
            if (sensor.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element sensorEl = (Element) sensor;
            Node id = sensorEl.getAttributeNode("id");
            Node value = sensorEl.getAttributeNode("value");

            ret.add(new Pair<>(id.getTextContent().trim(), Float.parseFloat(value.getTextContent().trim())));
        }
        return ret;
    }

}
