import parsers.ClientParser;
import parsers.NodeParser;
import sensor_network.PortName;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.ArrayList;

public class TestGenerator {

    public static void main(String[] args) throws JAXBException {
        stressTest1();
    }

    private static void stressTest1() throws JAXBException {
        // Nodes
        NodeParser.Forest forest = new NodeParser.Forest();
        forest.nodes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String nodeId = "node-" + i;
            forest.nodes.add(
                new NodeParser.Node(nodeId, "plugin-" + nodeId, new NodeParser.Threads(2, 2), 100, new NodeParser.Position(i, i), (long) (60 + (i * 10)),
                                    (long) (4000 + (60 + (i * 10)) * 2), 100L, new ArrayList<NodeParser.Sensor>() {{
                    add(new NodeParser.Sensor("temp", 10f, 2f));
                    add(new NodeParser.Sensor("humidity", 10f, 2f));
                }}, new ArrayList<NodeParser.Port>() {{
                    PortName requesting = PortName.REQUESTING;
                    PortName p2p = PortName.P2P;
                    add(new NodeParser.Port(requesting, nodeId + ":inbound:" + requesting.xmlName()));
                    add(new NodeParser.Port(p2p, nodeId + ":inbound:" + p2p.xmlName()));
                }}, new ArrayList<NodeParser.Port>() {{
                    PortName result = PortName.REQUEST_RESULT;
                    PortName p2p = PortName.P2P;
                    PortName registration = PortName.REGISTRATION;
                    PortName clock = PortName.CLOCK;
                    add(new NodeParser.Port(result, nodeId + ":outbound:" + result.xmlName()));
                    add(new NodeParser.Port(p2p, nodeId + ":outbound:" + p2p.xmlName()));
                    add(new NodeParser.Port(registration, nodeId + ":outbound:" + registration.xmlName()));
                    add(new NodeParser.Port(clock, nodeId + ":outbound:" + clock.xmlName()));
                }}));
        }

        JAXBContext forestCtx = JAXBContext.newInstance(NodeParser.Forest.class);
        Marshaller forestCtxMarshaller = forestCtx.createMarshaller();
        forestCtxMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        forestCtxMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        forestCtxMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        forestCtxMarshaller.marshal(forest, new File("forest.xml"));

        // Clients
        ClientParser.Clients clients = new ClientParser.Clients();
        clients.clients = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String clientId = "client-" + i;
            int finalI = i;
            clients.clients.add(
                new ClientParser.Client(
                    clientId, "plugin-" + clientId,
                    new NodeParser.Threads(2, 2),
                    new ArrayList<ClientParser.Target>() {{
                        String node1 = "node-" + finalI;
                        String node2 = "node-" + finalI + 1;
                        add(new ClientParser.Target(node1,
                                                    clientId + ":" + node1,
                                                    true,
                                                    "bool @temp > 10 dir ne 2",
                                                    100));
                        add(new ClientParser.Target(node2,
                                                    clientId + ":" + node2,
                                                    true,
                                                    "bool @temp > 10 dir ne 2",
                                                    100));
                    }},
                    100,
                    100,
                    new ArrayList<ClientParser.Port>() {{
                        PortName reqResult = PortName.REQUEST_RESULT;
                        add(new ClientParser.Port(reqResult, clientId + ":inbound:" + reqResult.xmlName()));
                    }},
                    new ArrayList<ClientParser.Port>() {{
                        PortName lookup = PortName.LOOKUP;
                        PortName clock = PortName.CLOCK;
                        add(new ClientParser.Port(lookup, clientId + ":outbound:" + lookup.xmlName()));
                        add(new ClientParser.Port(clock, clientId + ":outbound:" + clock.xmlName()));
                    }}
                )
            );
        }

        JAXBContext clientCtx = JAXBContext.newInstance(ClientParser.Clients.class);
        Marshaller clientCtxMarshaller = clientCtx.createMarshaller();
        clientCtxMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        clientCtxMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        clientCtxMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        clientCtxMarshaller.marshal(clients, new File("client.xml"));

        // tests


    }

}
