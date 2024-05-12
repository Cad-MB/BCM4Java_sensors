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


    /**
     * Constructor for the CVM class.
     * Initializes the CVM with configurations based on the provided configuration name.
     * Parses test configurations to set up the test suite for the simulation.
     *
     * @param configName The name of the configuration directory under the base path.
     * @throws Exception If there is an issue parsing the test configurations.
     */
    public CVM(String configName) throws Exception {
        System.setProperty("javax.xml.accessExternalDTD", "all");
        this.pathPrefix = Paths.get(basePath.toString(), configName);

        File testFile = Paths.get(pathPrefix.toString(), TEST_FILENAME).toFile();
        this.tests = TestParser.parse(testFile);
    }

    /**
     * Starts the standard lifecycle of the CVM with a predefined execution duration.
     * This method is typically used to run the simulation for the entire duration as specified by the test configurations.
     */
    public void deployWithConfigDelay() {
        this.startStandardLifeCycle(this.tests.executionDuration);
    }

    /**
     * Deploys the CVM components including the clock server, nodes, and clients.
     * This method sets up the entire simulation environment based on the parsed configuration files.
     *
     * @throws Exception If there is an issue deploying the components.
     */
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

    /**
     * Sets up the Clock Server component to synchronize time across the simulation.
     * The clock server is initialized with a start delay and an acceleration factor to manage simulated time.
     *
     * @throws Exception If there is an issue creating the Clock Server component.
     */
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

    /**
     * Sets up a Client component based on the provided client data and associated tests.
     * This method initializes a client in the simulation with its ports and scheduled tests.
     *
     * @param clientData The data for the client including ports and identifiers.
     * @param tests The tests associated with this client.
     * @throws Exception If there is an issue creating the Client component.
     */
    public void setupClient(ClientParser.Client clientData, List<TestParser.Test> tests) throws Exception {
        Object[] args = {
            clientData,
            clientData.inboundPorts.stream().collect(Collectors.toMap(port -> port.portName, port -> port.uri)),
            clientData.outboundPorts.stream().collect(Collectors.toMap(port -> port.portName, port -> port.uri)),
            tests
        };
        AbstractComponent.createComponent(Client.class.getCanonicalName(), args);
    }

    /**
     * Sets up a Node component based on the provided node data.
     * This method initializes a node in the simulation with its ports and configurations.
     *
     * @param nodeData The data for the node including ports and identifiers.
     * @throws Exception If there is an issue creating the Node component.
     */
    public void setupNode(NodeParser.Node nodeData) throws Exception {
        Object[] componentArgs = {
            nodeData,
            nodeData.inboundPorts.stream().collect(Collectors.toMap(port -> port.portName, port -> port.uri)),
            nodeData.outboundPorts.stream().collect(Collectors.toMap(port -> port.portName, port -> port.uri)),
        };
        AbstractComponent.createComponent(Node.class.getCanonicalName(), componentArgs);
    }

}
