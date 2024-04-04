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
import javafx.util.Pair;
import parsers.client.ClientParsedData;
import parsers.client.ClientXMLParser;
import parsers.node.NodeParsedData;
import parsers.node.NodeXMLParser;
import parsers.query.QueryParser;
import parsers.query.Result;
import sensor_network.NodeInfo;
import sensor_network.SensorData;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CVM
    extends AbstractCVM {

    // region fields
    public static final String CLOCK_URI = "global-clock-uri";
    protected static final Path basePath = Paths.get("src", "main", "resources", "configs");
    protected static final String FOREST_FILENAME = "forest.xml";
    protected static final String CLIENT_FILENAME = "client.xml";

    protected final Set<SensorDataI> sensorsAll;
    protected final HashMap<String, Set<SensorDataI>> sensorInfoMap;
    protected final String configName;
    // endregion


    public CVM(String configName, HashSet<SensorDataI> sensorsAll) throws Exception {
        this.sensorsAll = sensorsAll;
        this.sensorInfoMap = new HashMap<>();
        this.configName = configName;
        new SensorRandomizer(sensorsAll).start();
    }

    public CVM(Set<SensorDataI> sensorsAll, HashMap<String, Set<SensorDataI>> sensorInfoMap, String configName)
        throws Exception {
        this.sensorsAll = sensorsAll;
        this.sensorInfoMap = sensorInfoMap;
        this.configName = configName;
        new SensorRandomizer(sensorsAll).start();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("donnée un nom de config");
            System.exit(1);
        }
        HashSet<SensorDataI> sensorsAll = new HashSet<>();
        CVM c = new CVM(args[0], sensorsAll);
        c.startStandardLifeCycle(20000000L);
        SensorRandomizer randomizer = new SensorRandomizer(sensorsAll);
        randomizer.start();
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

        Set<SensorDataI> sensors = new HashSet<>();
        for (Pair<String, Float> parsedSensor : nodeParsedData.sensors) {
            sensors.add(new SensorData<>(
                nodeInfo.nodeIdentifier(),
                parsedSensor.getKey(),
                parsedSensor.getValue(),
                Instant.now()
            ));
        }

        Object[] componentArgs = { nodeInfo, sensors, nodeParsedData.delay };
        String nodeUri = AbstractComponent.createComponent(Node.class.getCanonicalName(), componentArgs);

        doPortConnection(
            nodeUri,
            Node.OUTBOUND_URI.REGISTRY.of(nodeInfo),
            Registry.INBOUND_URI.NODE.uri,
            ConnectorNodeRegistry.class.getCanonicalName()
        );
        synchronized (this) {
            sensorsAll.addAll(sensors);
            sensorInfoMap.put(nodeInfo.nodeIdentifier(), sensors);
        }
    }


    public interface CallbackI {

        void callback(String id, Object data);

    }


    public static class SensorRandomizer
        extends Thread {

        private final Set<SensorDataI> sensors;
        private final Random random;
        private boolean hasCallback = false;
        private CallbackI callback;

        public SensorRandomizer(Set<SensorDataI> sensors) {
            this.sensors = sensors;
            this.random = new Random();
        }


        public void setCallback(CallbackI fn) {
            hasCallback = true;
            callback = fn;
        }

        @Override
        public void run() {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Set<SensorDataI> newSensors = new HashSet<>();
                    for (SensorDataI sensor : sensors) {

                        Serializable oldValue = sensor.getValue();
                        assert oldValue instanceof Boolean || oldValue instanceof Number;

                        Serializable newValue;
                        double toAdd = (random.nextDouble() * .1) - .05;
                        if (oldValue instanceof Boolean) {
                            newValue = !(Boolean) oldValue;
                        } else {
                            newValue = ((Number) oldValue).doubleValue() + toAdd;
                        }

                        SensorData<Serializable> newSensorData =
                            new SensorData<>(sensor.getNodeIdentifier(),
                                             sensor.getSensorIdentifier(),
                                             newValue,
                                             Instant.now());
                        newSensors.add(newSensorData);
                        if (hasCallback) {
                            callback.callback(sensor.getSensorIdentifier(), newSensorData);
                        }
                    }
                    synchronized (this) {
                        sensors.clear();
                        sensors.addAll(newSensors);
                    }
                }
            }, 0, 2000);
        }

    }

}

