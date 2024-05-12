package test_generators;

import parsers.ClientParser;
import parsers.NodeParser;
import parsers.TestParser;
import sensor_network.PortName;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class TestThreadsByThreadCount {

    public static void main(String[] args) throws JAXBException {
        // variation du nombre de threads
        int[] threadCounts = {1, 2, 5, 10};
        for (int count : threadCounts) {
            clientStressTestByThreads(count);
        }
    }

    private static void clientStressTestByThreads(int threadCount) throws JAXBException {
        String baseDir = "src/main/resources/configs/client-stress-test-threads-" + threadCount + "/";
        new File(baseDir).mkdirs();
        // Nodes
        NodeParser.Forest forest = new NodeParser.Forest();
        forest.nodes = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            String nodeId = "node-" + i;
            forest.nodes.add(
                new NodeParser.Node(
                    nodeId,
                    "plugin-" + nodeId,
                    new NodeParser.Threads(threadCount, threadCount),
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
                    }}
                ));
        }
        TestGenerator.outputForest(baseDir, forest);

        // Clients
        ClientParser.Clients clients = new ClientParser.Clients();
        clients.clients = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String clientId = "client-" + i;
            int finalI = i;
            clients.clients.add(
                new ClientParser.Client(
                    clientId,
                    "plugin-" + clientId,
                    600,
                    new NodeParser.Threads(threadCount, threadCount),
                    new ArrayList<ClientParser.Target>() {{
                        String node1 = "node-1";
                        String node2 = "node-2";
                        add(new ClientParser.Target(node1,
                                                    clientId + ":" + node1,
                                                    true,
                                                    "bool @temp > 10 dir ne 2",
                                                    600 + (finalI * 10)));
                        add(new ClientParser.Target(node2,
                                                    clientId + ":" + node2,
                                                    true,
                                                    "bool @temp > 10 dir ne 2",
                                                    601 + (finalI * 10)));
                    }},
                    80,
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

        TestGenerator.outputClient(baseDir, clients);

        // Tests
        parsers.TestParser.Tests tests = new TestParser.Tests();
        tests.testList = new ArrayList<>();
        tests.executionDuration = 30000L;


        for (int i = 0; i < 3; i++) {
            String clientId = "client-" + i;
            String node1 = "node-" + i;
            String node2 = "node-" + (i + 1);
            tests.testList.add(new TestParser.Test(
                clientId + ":test" + i,
                clientId,
                clientId + "-0",
                550 + (i * 100),
                true,
                Arrays.asList(node1, node2),
                Arrays.asList(
                    new TestParser.GatherResult("temp", node1, 10.0 + i),
                    new TestParser.GatherResult("humidity", node2, 10.0 + i)
                )
            ));
        }
        TestGenerator.outputTests(baseDir, tests);

    }

}
