package cvm;

import ast.query.Query;
import components.ConnectorClientRegistry;
import components.ConnectorNodeRegistry;
import components.client.Client;
import components.node.Node;
import components.registry.Registry;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import parsers.client.ClientParsedData;
import parsers.client.ClientXMLParser;
import parsers.node.NodeParsedData;
import parsers.node.NodeXMLParser;
import parsers.query.QueryParser;
import parsers.query.Result;
import sensor_network.NodeInfo;
import sensor_network.SensorData;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CVM
    extends AbstractCVM {

    public static final String CLOCK_URI = "global-clock-uri";
    protected static final Path basePath = Paths.get("src", "main", "resources", "configs");
    protected static final String FOREST_FILENAME = "forest.xml";
    protected static final String CLIENT_FILENAME = "client.xml";

    protected final String configName;


    public CVM(String configName) throws Exception {
        this.configName = configName;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("donnée un nom de config");
            System.exit(1);
        }
        CVM c = new CVM(args[0]);
        c.startStandardLifeCycle(20000000L);
        System.exit(0);
    }

    @Override
    public void deploy() throws Exception {
        super.deploy();

        Path pathPrefix = Paths.get(basePath.toString(), this.configName);

        File foretFile = Paths.get(pathPrefix.toString(), FOREST_FILENAME).toFile();
        File clientFile = Paths.get(pathPrefix.toString(), CLIENT_FILENAME).toFile();

        ArrayList<NodeParsedData> nodeDataList = NodeXMLParser.parse(foretFile);
        ArrayList<ClientParsedData> clientDataList = ClientXMLParser.parse(clientFile);

        setupClockServer();
        AbstractComponent.createComponent(Registry.class.getCanonicalName(), new Object[]{});

        for (NodeParsedData nodeParsedData : nodeDataList) {
            setupNode(nodeParsedData);
        }

        for (ClientParsedData client : clientDataList) {
            setupClient(client);
        }

    }

    private void setupClient(ClientParsedData clientParsedData) throws Exception {
        QueryParser parser = new QueryParser();

        List<Query> queries = clientParsedData.queries
            .stream()
            .map(parser::parseQuery)
            .filter(Result::isParsed)
            .map(Result::parsed)
            .collect(Collectors.toList());

        Object[] args = { clientParsedData.id, clientParsedData.targetNodesIds, queries, clientParsedData.frequency };
        String clientURI = AbstractComponent.createComponent(Client.class.getCanonicalName(), args);

        doPortConnection(
            clientURI,
            Client.uri(Client.OUTBOUND_URI.REGISTRY, clientParsedData.id),
            Registry.INBOUND_URI.CLIENT.uri,
            ConnectorClientRegistry.class.getCanonicalName()
        );
    }

    private void setupClockServer() throws Exception {
        Instant instant = Instant.parse("2024-01-31T09:00:00.00Z");
        long startDelay = 5000L;
        double accelerationFactor = 60d; // 1 minute (simulated) = 1 second (real)
        AbstractComponent.createComponent(ClocksServer.class.getCanonicalName(), new Object[]{
            CLOCK_URI,
            TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis() + startDelay),
            instant,
            accelerationFactor
        });
    }

    public void setupNode(NodeParsedData nodeParsedData) throws Exception {
        NodeInfo nodeInfo = new NodeInfo(nodeParsedData.range, nodeParsedData.id, nodeParsedData.position);

        HashMap<SensorDataI, Float> sensors = new HashMap<>();
        for (NodeParsedData.SensorParsedData parsedSensor : nodeParsedData.sensors) {
            sensors.put(
                new SensorData<>(nodeInfo.nodeIdentifier(), parsedSensor.id, parsedSensor.startingValue, Instant.now()),
                parsedSensor.toAdd
            );
        }

        Object[] componentArgs = { nodeInfo, sensors, nodeParsedData.delay };
        String nodeUri = AbstractComponent.createComponent(Node.class.getCanonicalName(), componentArgs);

        doPortConnection(
            nodeUri,
            Node.OUTBOUND_URI.REGISTRY.of(nodeInfo),
            Registry.INBOUND_URI.NODE.uri,
            ConnectorNodeRegistry.class.getCanonicalName()
        );
    }

}

