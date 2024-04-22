package cvm;

import components.client.Client;
import components.node.Node;
import components.registry.Registry;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import parsers.ClientParser;
import parsers.NodeParser;
import parsers.TestParser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CVM
    extends AbstractCVM {

    public static final String CLOCK_URI = "global-clock-uri";
    public static final Instant START_INSTANT = Instant.parse("2024-01-01T00:00:00.00Z");

    protected static final Path basePath = Paths.get("src", "main", "resources", "configs");
    protected static final String FOREST_FILENAME = "forest.xml";
    protected static final String CLIENT_FILENAME = "client.xml";
    protected static final String TEST_FILENAME = "tests.xml";

    protected final Path pathPrefix;
    protected final TestParser.Tests tests;


    public CVM(String configName) throws Exception {
        this.pathPrefix = Paths.get(basePath.toString(), configName);

        File testFile = Paths.get(pathPrefix.toString(), TEST_FILENAME).toFile();
        this.tests = TestParser.parse(testFile);
    }

    public void deployWithConfigDelay() {
        this.startStandardLifeCycle(30000L);
    }

    @Override
    public void deploy() throws Exception {
        super.deploy();

        File forestFile = Paths.get(pathPrefix.toString(), FOREST_FILENAME).toFile();
        File clientFile = Paths.get(pathPrefix.toString(), CLIENT_FILENAME).toFile();

        ArrayList<NodeParser.Node> nodeDataList = NodeParser.parse(forestFile);
        ArrayList<ClientParser.Client> clientDataList = ClientParser.parse(clientFile);
        Map<String, List<TestParser.Test>> testMap =
            tests.testList.stream()
                          .collect(Collectors.toMap(
                              test -> test.clientId,
                              test -> tests.testList.stream().filter(pt -> Objects.equals(pt.clientId, test.clientId)).collect(Collectors.toList()),
                              (test1, test2) -> test1
                          ));

        setupClockServer();
        AbstractComponent.createComponent(Registry.class.getCanonicalName(), new Object[]{});

        for (NodeParser.Node nodeParsedData : nodeDataList) {
            setupNode(nodeParsedData);
        }

        for (ClientParser.Client clientData : clientDataList) {
            setupClient(clientData, testMap.getOrDefault(clientData.id, new ArrayList<>()));
        }

    }

    private void setupClockServer() throws Exception {
        long startDelay = 5000L;
        double accelerationFactor = 60d; // 1 minute (simulated) = 1 second (real)
        AbstractComponent.createComponent(ClocksServer.class.getCanonicalName(), new Object[]{
            CLOCK_URI,
            TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis() + startDelay),
            START_INSTANT,
            accelerationFactor
        });
    }

    public void setupClient(ClientParser.Client clientData, List<TestParser.Test> tests) throws Exception {
        Object[] args = {
            clientData,
            clientData.inboundPorts.stream().collect(Collectors.toMap(port -> port.portName, port -> port.uri)),
            clientData.outboundPorts.stream().collect(Collectors.toMap(port -> port.portName, port -> port.uri)),
            tests
        };
        AbstractComponent.createComponent(Client.class.getCanonicalName(), args);
    }

    public void setupNode(NodeParser.Node nodeData) throws Exception {
        Object[] componentArgs = {
            nodeData,
            nodeData.inboundPorts.stream().collect(Collectors.toMap(port -> port.portName, port -> port.uri)),
            nodeData.outboundPorts.stream().collect(Collectors.toMap(port -> port.portName, port -> port.uri)),
        };
        AbstractComponent.createComponent(Node.class.getCanonicalName(), componentArgs);
    }

}
