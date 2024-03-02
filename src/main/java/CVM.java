import components.ConnectorClientRegistry;
import components.ConnectorNodeRegistry;
import components.client.Client;
import components.node.Node;
import components.registry.Registry;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import requests.NodeInfo;
import requests.Position;
import requests.SensorData;
import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.*;

public class CVM
    extends AbstractCVM {

    public CVM() throws Exception {
    }

    public static void main(String[] args) throws Exception {
        CVM c = new CVM();
        c.startStandardLifeCycle(200000L);
        System.exit(0);
    }

    public void staticTest() throws Exception {
        AbstractComponent.createComponent(Registry.class.getCanonicalName(), new Object[]{});

        NodeInfo nodeInfo1 = new NodeInfo(100, "node1", new Position(1d, 2d));
        Set<SensorDataI> sensors1 = new HashSet<>();
        sensors1.add(new SensorData<>(nodeInfo1.nodeIdentifier(), "sensor1", 100d, Instant.now()));
        NodeInfo nodeInfo2 = new NodeInfo(200, "node2", new Position(2d, 3d));
        Set<SensorDataI> sensors2 = new HashSet<>();
        sensors2.add(new SensorData<>(nodeInfo2.nodeIdentifier(), "sensor1", 150d, Instant.now()));

        String clientURI = AbstractComponent.createComponent(Client.class.getCanonicalName(), new Object[]{});
        String node1URI = AbstractComponent.createComponent(Node.class.getCanonicalName(),
                                                            new Object[]{ nodeInfo1, sensors1 });
        String node2URI = AbstractComponent.createComponent(Node.class.getCanonicalName(),
                                                            new Object[]{ nodeInfo2, sensors2 });

        this.doPortConnection(
            node1URI,
            Node.uri(Node.OUTBOUND_URI.REGISTRY, nodeInfo1),
            Registry.INBOUND_URI.NODE.uri,
            ConnectorNodeRegistry.class.getCanonicalName()
        );

        this.doPortConnection(
            node2URI,
            Node.OUTBOUND_URI.REGISTRY.uri + "-" + nodeInfo2.nodeIdentifier(),
            Registry.INBOUND_URI.NODE.uri,
            ConnectorNodeRegistry.class.getCanonicalName()
        );

        Thread.sleep(2000);
        this.doPortConnection(
            clientURI,
            Client.OUTBOUND_URI.REGISTRY.uri,
            Registry.INBOUND_URI.CLIENT.uri,
            ConnectorClientRegistry.class.getCanonicalName()
        );
    }

    @Override
    public void deploy() throws Exception {
        super.deploy();
        URL fileUrl = getClass().getClassLoader().getResource("json/foret3(small).json");
        assert fileUrl != null;

        ArrayList<ParsedData.Node> nodeDataList = JsonParser.parse(new File(fileUrl.toURI()));

        AbstractComponent.createComponent(Registry.class.getCanonicalName(), new Object[]{});
        String clientURI = AbstractComponent.createComponent(Client.class.getCanonicalName(), new Object[]{});

        Set<SensorDataI> sensorsAll = new HashSet<>();
        for (ParsedData.Node parsedData : nodeDataList) {
            Position nodePos = new Position(parsedData.position.x, parsedData.position.y);
            NodeInfo nodeInfo = new NodeInfo(parsedData.range, parsedData.id, nodePos);

            Set<SensorDataI> sensors = new HashSet<>();
            for (ParsedData.Sensor parsedSensor : parsedData.sensors) {
                // todo: add date
                sensors.add(new SensorData<>(
                        nodeInfo.nodeIdentifier(),
                        parsedSensor.id,
                        parsedSensor.value,
                        Instant.now()
                ));
            }

            Object[] componentArgs = {nodeInfo, sensors};
            String nodeUri = AbstractComponent.createComponent(Node.class.getCanonicalName(), componentArgs);

            doPortConnection(
                    nodeUri,
                    Node.uri(Node.OUTBOUND_URI.REGISTRY, nodeInfo),
                    Registry.INBOUND_URI.NODE.uri,
                    ConnectorNodeRegistry.class.getCanonicalName()
            );
            sensorsAll.addAll(sensors);
        }

        doPortConnection(
                clientURI,
                Client.OUTBOUND_URI.REGISTRY.uri,
                Registry.INBOUND_URI.CLIENT.uri,
                ConnectorClientRegistry.class.getCanonicalName()
        );

        SensorRandomizer randomizer = new SensorRandomizer(sensorsAll);
        randomizer.start();
    }

    public static class SensorRandomizer extends Thread {
        private final Set<SensorDataI> sensors;
        private final Random random;

        public SensorRandomizer(Set<SensorDataI>sensors) {
            this.sensors = sensors;
            this.random = new Random();
        }

        @Override
        public void run() {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {

                    for (SensorDataI sensor : sensors) {
                        double newValue = random.nextDouble() * 100;
                        assert sensor instanceof SensorData;
                        //noinspection unchecked
                        ((SensorData<Double>) sensor).setValue(newValue);
                        // System.out.println("Sensor " + sensor.getSensorIdentifier() + " value updated: " + newValue);
                    }
                }
            }, 0, 1000);
        }

    }


}
