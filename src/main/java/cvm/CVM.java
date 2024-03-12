package cvm;

import ast.bexp.CExpBExp;
import ast.cexp.LCExp;
import ast.cont.DCont;
import ast.cont.ECont;
import ast.dirs.FDirs;
import ast.gather.FGather;
import ast.query.BQuery;
import ast.query.GQuery;
import ast.query.Query;
import ast.rand.CRand;
import ast.rand.SRand;
import components.ConnectorClientRegistry;
import components.ConnectorNodeRegistry;
import components.client.Client;
import components.node.Node;
import components.registry.Registry;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import parser.client.ClientParsedData;
import parser.query.QueryParser;
import parser.tree.TreeJsonParser;
import parser.tree.TreeParsedData;
import requests.NodeInfo;
import requests.Position;
import requests.SensorData;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CVM
    extends AbstractCVM {

    // region fields
    public static final String CLOCK_URI = "global-clock-uri";
    private final Set<SensorDataI> sensorsAll;
    private CallbackI notifier;
    private final HashMap<String, Set<SensorDataI>> sensorInfoMap;
    // endregion


    public CVM() throws Exception {
        sensorsAll = new HashSet<>();
        sensorInfoMap = new HashMap<>();
    }

    public CVM(Set<SensorDataI> sensorsAll, HashMap<String, Set<SensorDataI>> sensorInfoMap, CallbackI notifier)
        throws Exception {
        this.sensorsAll = sensorsAll;
        this.sensorInfoMap = sensorInfoMap;
        this.notifier = notifier;
    }

    public static void main(String[] args) throws Exception {
        CVM c = new CVM();
        c.startStandardLifeCycle(20000000L);
        System.exit(0);
    }

    @Override
    public void deploy() throws Exception {
        super.deploy();
        URL treeFileUrl = getClass().getClassLoader().getResource("json/foret(small3).json");
        assert treeFileUrl != null;
        URL clientFileUrl = getClass().getClassLoader().getResource("json/client.json");
        assert clientFileUrl != null;

        ArrayList<TreeParsedData.Node> nodeDataList = TreeJsonParser.parse(new File(treeFileUrl.toURI()));
        // ArrayList<ClientParsedData.Client> clientDataList = ClientJsonParser.parse(new File(clientFileUrl.toURI()));


        setupClockServer();
        AbstractComponent.createComponent(Registry.class.getCanonicalName(), new Object[]{});

        Query gQuery1 = new GQuery(new FGather("temp"), new DCont(new FDirs(Direction.NE), 3));

        Query gQuery2 = new BQuery(
            new CExpBExp(
                new LCExp(
                    new SRand("temp"),
                    new CRand(30d)
                ))
            , new ECont());


        ArrayList<String> nodeIds1 = new ArrayList<>();
        nodeIds1.add("node1");
        QueryParser parser = new QueryParser();
        ArrayList<Query> queries = new ArrayList<>();
        queries.add(parser.parseQuery("bool ((@temp<30)) (empty)").parsed());

        String clientURI1 = AbstractComponent.createComponent(Client.class.getCanonicalName(),
                                                              new Object[]{ nodeIds1, queries, 10000 });
        // Client.class.getConstructor(new Object[] {nodeIds1})
        // String clientURI2 = AbstractComponent.createComponent(Client.class.getCanonicalName(),
        //                                                       new Object[]{ "node3", gQuery2 });

        for (TreeParsedData.Node nodeParsedData : nodeDataList) {
            setupNode(nodeParsedData);
        }

        // for (int i = 0; i < clientDataList.size(); i++) {
        //     ClientParsedData.Client clientParsedData = clientDataList.get(i);
        //     setupClient(clientParsedData, i);
        // }

        doPortConnection(
            clientURI1,
            Client.uri(Client.OUTBOUND_URI.REGISTRY, 0),
            Registry.INBOUND_URI.CLIENT.uri,
            ConnectorClientRegistry.class.getCanonicalName()
        );
        // doPortConnection(
        //     clientURI2,
        //     Client.uri(Client.OUTBOUND_URI.REGISTRY, 1),
        //     Registry.INBOUND_URI.CLIENT.uri,
        //     ConnectorClientRegistry.class.getCanonicalName()
        // );
    }

    private void setupClient(ClientParsedData.Client clientParsedData, int i) throws Exception {
        // todo: passage de query string a query Query
        // clientParsedData.queries -> queries_parsed
        ArrayList<Query> queries_parsed = null;

        String clientURI = AbstractComponent.createComponent(Client.class.getCanonicalName(),
                                                             new Object[]{ clientParsedData.target_nodes_ids,
                                                                 queries_parsed, clientParsedData.frequency });

        doPortConnection(
            clientURI,
            Client.uri(Client.OUTBOUND_URI.REGISTRY, i),
            Registry.INBOUND_URI.CLIENT.uri,
            ConnectorClientRegistry.class.getCanonicalName()
        );
    }

    private void setupClockServer() throws Exception {
        Instant instant = Instant.parse("2024-01-31T09:00:00.00Z");
        long startDelay = 5000L;
        double accelerationFactor = 60d; // 1 sec / minute
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
            if (notifier != null) {
                notifier.callback(nodeInfo.nodeIdentifier(), nodeInfo);
            }
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
                        double newValue;
                        if (oldValue instanceof Float) {
                            newValue = ((float) sensor.getValue()) + ((random.nextDouble() * 20) - 10); // -10 - 10
                        } else {
                            newValue = ((double) sensor.getValue()) + ((random.nextDouble() * 20) - 10);
                        }

                        assert sensor instanceof SensorData;
                        // noinspection unchecked
                        ((SensorData<Double>) sensor).setValue(newValue);
                        if (hasCallback) {
                            callback.callback(sensor.getSensorIdentifier(), sensor);
                        }
                    }
                }
            }, 0, 1000);
        }

    }

}

