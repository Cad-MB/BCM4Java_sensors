package test_generators;

import parsers.ClientParser;
import parsers.NodeParser;
import parsers.TestParser;
import sensor_network.PortName;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class TestGenerator {

    public static void main(String[] args) throws JAXBException {
        // stressTest1();
        // stressTest2();
    }

    private static void stressTest1() throws JAXBException {
        String baseDir = "src/main/resources/configs/stress_test_1/";
        new File(baseDir).mkdirs();
        // Nodes
        NodeParser.Forest forest = new NodeParser.Forest();
        forest.nodes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String nodeId = "node-" + i;
            forest.nodes.add(
                new NodeParser.Node(
                    nodeId,
                    "plugin-" + nodeId,
                    new NodeParser.Threads(2, 2),
                    200,
                    new NodeParser.Position((i + 1) * 100, (i + 1) * 100),
                    400 + (long) (60 + (i * 10)),
                    (long) (4000 + (60 + (i * 10)) * 2), 100L,
                    new ArrayList<NodeParser.Sensor>() {{
                        add(new NodeParser.Sensor("temp", 20f, 2f));
                        add(new NodeParser.Sensor("humidity", 20f, 2f));
                    }},
                    new ArrayList<NodeParser.Port>() {{
                        PortName requesting = PortName.REQUESTING;
                        PortName p2p = PortName.P2P;
                        add(new NodeParser.Port(requesting, nodeId + ":inbound:" + requesting.xmlName()));
                        add(new NodeParser.Port(p2p, nodeId + ":inbound:" + p2p.xmlName()));
                    }},
                    new ArrayList<NodeParser.Port>() {{
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

        outputForest(baseDir, forest);

        // Clients
        ClientParser.Clients clients = new ClientParser.Clients();
        clients.clients = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String clientId = "client-" + i;
            int finalI = i;
            clients.clients.add(
                new ClientParser.Client(
                    clientId,
                    "plugin-" + clientId,
                    20,
                    new NodeParser.Threads(2, 2),
                    new ArrayList<ClientParser.Target>() {{
                        String node1 = "node-" + finalI;
                        String node2 = "node-" + (finalI + 1);
                        add(new ClientParser.Target(node1,
                                                    clientId + ":" + node1,
                                                    true,
                                                    "bool @temp > 10 dir ne 2",
                                                    600));
                        add(new ClientParser.Target(node2,
                                                    clientId + ":" + node2,
                                                    true,
                                                    "bool @temp > 10 dir ne 2",
                                                    601));
                    }},
                    100,
                    2000,
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

        outputClient(baseDir, clients);

        // Tests
        TestParser.Tests tests = new TestParser.Tests();
        tests.testList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            String clientId = "client-" + i;
            String node1 = "node-" + i;
            String node2 = "node-" + i + 1;
            tests.testList.add(new TestParser.Test(
                clientId + ":test" + i,
                clientId,
                clientId + "-0",
                550 + (i * 100),
                true, // a corriger
                Arrays.asList(node1, node2),
                null
                // Arrays.asList(
                //     new TestParser.GatherResult("temp", node1, 10.0 + i),
                //     new TestParser.GatherResult("humidity", node2, 10.0 + i)
                // )
            ));
        }
        outputTests(baseDir, tests);
    }


    private static void stressTest2() throws JAXBException {
        String baseDir = "src/main/resources/configs/stress_test_2/";
        new File(baseDir).mkdirs();

        int gridWidth = 10;

        // Nodes
        NodeParser.Forest forest = new NodeParser.Forest();
        forest.nodes = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            int x;
            int y = i / 10;
            if (y % 2 == 0) { x = ((i % 10) * 2) + 1; } else { x = (i % 10) * 2; }

            String nodeId = "node-" + i;
            forest.nodes.add(new NodeParser.Node(
                nodeId, "plugin-" + nodeId,
                new NodeParser.Threads(2, 2), 200,
                new NodeParser.Position(x * 100, y * 100), (long) (400 + (i * 10)),
                (long) (4000 + (60 + (i * 10)) * 2), 100L,
                new ArrayList<NodeParser.Sensor>() {{
                    add(new NodeParser.Sensor("temp", 10f, 2f));
                    add(new NodeParser.Sensor("humidity", 10f, 2f));
                }},
                new ArrayList<NodeParser.Port>() {{
                    PortName requesting = PortName.REQUESTING;
                    PortName p2p = PortName.P2P;
                    add(new NodeParser.Port(requesting, nodeId + ":inbound:" + requesting.xmlName()));
                    add(new NodeParser.Port(p2p, nodeId + ":inbound:" + p2p.xmlName()));
                }},
                new ArrayList<NodeParser.Port>() {{
                    PortName result = PortName.REQUEST_RESULT;
                    PortName p2p = PortName.P2P;
                    PortName registration = PortName.REGISTRATION;
                    PortName clock = PortName.CLOCK;
                    add(new NodeParser.Port(result, nodeId + ":outbound:" + result.xmlName()));
                    add(new NodeParser.Port(p2p, nodeId + ":outbound:" + p2p.xmlName()));
                    add(new NodeParser.Port(registration, nodeId + ":outbound:" + registration.xmlName()));
                    add(new NodeParser.Port(clock, nodeId + ":outbound:" + clock.xmlName()));
                }}
            ));
        }

        outputForest(baseDir, forest);

        // Clients
        ClientParser.Clients clients = new ClientParser.Clients();
        clients.clients = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String clientId = "client-" + i;
            int finalI = i;
            clients.clients.add(new ClientParser.Client(
                clientId,
                "plugin-" + clientId,
                20,
                new NodeParser.Threads(2, 2),
                new ArrayList<ClientParser.Target>() {{
                    String node1 = "node-" + (finalI * 10);
                    String node2 = "node-" + (finalI * 10 + 1);
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
            ));
        }

        outputClient(baseDir, clients);

        // Tests
        TestParser.Tests tests = new TestParser.Tests();
        tests.testList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            String clientId = "client-" + i;
            String node1 = "node-" + (i * 10);
            String node2 = "node-" + (i * 10 + 1);
            tests.testList.add(new TestParser.Test(
                clientId + ":test" + i,
                clientId,
                clientId + "-0",
                550 + (i * 100),
                true,
                Arrays.asList(node1, node2),
                null
            ));
        }

        outputTests(baseDir, tests);
    }


    public static void outputForest(String baseDir, NodeParser.Forest forest) throws JAXBException {
        JAXBContext forestCtx = JAXBContext.newInstance(NodeParser.Forest.class);
        Marshaller forestCtxMarshaller = forestCtx.createMarshaller();
        forestCtxMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        forestCtxMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        forestCtxMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        forestCtxMarshaller.marshal(forest, new File(baseDir + "forest.xml"));
    }

    public static void outputClient(String baseDir, ClientParser.Clients clients) throws JAXBException {

        JAXBContext clientCtx = JAXBContext.newInstance(ClientParser.Clients.class);
        Marshaller clientCtxMarshaller = clientCtx.createMarshaller();
        clientCtxMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        clientCtxMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        clientCtxMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        clientCtxMarshaller.marshal(clients, new File(baseDir + "client.xml"));

    }

    public static void outputTests(String baseDir, TestParser.Tests tests) throws JAXBException {
        JAXBContext testCtx = JAXBContext.newInstance(TestParser.Tests.class);
        Marshaller testMarshaller = testCtx.createMarshaller();
        testMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        testMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        testMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

        testMarshaller.marshal(tests, new File(baseDir + "tests.xml"));
    }

}
