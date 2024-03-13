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
import parser.client.ClientJsonParser;
import parser.client.ClientParsedData;
import parser.query.QueryParser;
import parser.query.Result;
import parser.tree.TreeJsonParser;
import parser.tree.TreeParsedData;
import requests.NodeInfo;
import requests.Position;
import requests.SensorData;

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
    private final Set<SensorDataI> sensorsAll;
    private final HashMap<String, Set<SensorDataI>> sensorInfoMap;
    // endregion


    public CVM() throws Exception {
        sensorsAll = new HashSet<>();
        sensorInfoMap = new HashMap<>();
    }

    public CVM(Set<SensorDataI> sensorsAll, HashMap<String, Set<SensorDataI>> sensorInfoMap)
        throws Exception {
        this.sensorsAll = sensorsAll;
        this.sensorInfoMap = sensorInfoMap;
    }

    public static void main(String[] args) throws Exception {
        HashSet<SensorDataI> sensorsAll = new HashSet<>();
        CVM c = new CVM(sensorsAll, new HashMap<>());
        c.startStandardLifeCycle(20000000L);
        SensorRandomizer randomizer = new SensorRandomizer(sensorsAll);
        randomizer.start();
        System.exit(0);
    }

    @Override
    public void deploy() throws Exception {
        super.deploy();

        Path pathPrefix = Paths.get("src", "main", "resources", "json", "small3");

        File foretFile = new File(pathPrefix + "/foret.json");
        File clientFile = new File(pathPrefix + "/client.json");

        ArrayList<TreeParsedData.Node> nodeDataList = TreeJsonParser.parse(foretFile);
        ArrayList<ClientParsedData.Client> clientDataList = ClientJsonParser.parse(clientFile);

        setupClockServer();
        AbstractComponent.createComponent(Registry.class.getCanonicalName(), new Object[]{});

        for (TreeParsedData.Node nodeParsedData : nodeDataList) {
            setupNode(nodeParsedData);
        }

        for (ClientParsedData.Client client : clientDataList) {
            setupClient(client);
        }
    }

    private void setupClient(ClientParsedData.Client clientParsedData) throws Exception {
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

    public void setupNode(TreeParsedData.Node nodeParsedData) throws Exception {
        Position nodePos = new Position(nodeParsedData.position.x, nodeParsedData.position.y);
        NodeInfo nodeInfo = new NodeInfo(nodeParsedData.range, nodeParsedData.id, nodePos);

        Set<SensorDataI> sensors = new HashSet<>();
        for (TreeParsedData.Sensor parsedSensor : nodeParsedData.sensors) {
            // todo: add date
            sensors.add(new SensorData<>(
                nodeInfo.nodeIdentifier(),
                parsedSensor.id,
                parsedSensor.value,
                Instant.now()
            ));
        }

        Object[] componentArgs = { nodeInfo, sensors };
        String nodeUri = AbstractComponent.createComponent(Node.class.getCanonicalName(), componentArgs);

        doPortConnection(
            nodeUri,
            Node.uri(Node.OUTBOUND_URI.REGISTRY, nodeInfo),
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
                    for (SensorDataI sensor : sensors) {
                        Serializable oldValue = sensor.getValue();
                        assert oldValue instanceof Boolean || oldValue instanceof Number;

                        Serializable newValue;
                        double toAdd = (random.nextDouble() * 20) - 10;
                        if (oldValue instanceof Boolean) {
                            newValue = !(Boolean) oldValue;
                        } else {
                            newValue = ((Number) oldValue).doubleValue() + toAdd;
                        }

                        // noinspection unchecked
                        ((SensorData<Serializable>) sensor).setValue(newValue);
                        if (hasCallback) {
                            callback.callback(sensor.getSensorIdentifier(), sensor);
                        }
                    }
                }
            }, 0, 1000);
        }

    }

}

