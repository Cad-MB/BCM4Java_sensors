import components.ConnectorClientRegistry;
import components.ConnectorNodeRegistry;
import components.client.Client;
import components.node.Node;
import components.registry.Registry;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
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

    public static final String CLOCK_URI = "global-clock-uri";
    public Set<SensorDataI> sensorsAll = new HashSet<>();
    public Set<NodeInfoI> nodeInfosAll = new HashSet<>();

    public CVM() throws Exception {
    }

    public static void main(String[] args) throws Exception {
        CVM c = new CVM();
        c.startStandardLifeCycle(20000000L);
        System.exit(0);
    }

    @Override
    public void deploy() throws Exception {
        super.deploy();
        URL fileUrl = getClass().getClassLoader().getResource("json/foret3(small).json");
        assert fileUrl != null;

        ArrayList<ParsedData.Node> nodeDataList = JsonParser.parse(new File(fileUrl.toURI()));

        setupClockServer();
        AbstractComponent.createComponent(Registry.class.getCanonicalName(), new Object[]{});
        String clientURI = AbstractComponent.createComponent(Client.class.getCanonicalName(),
                                                             new Object[]{ CLOCK_URI });

        for (ParsedData.Node parsedData : nodeDataList) {
            setupNode(parsedData);
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

    private void setupClockServer() throws Exception {
        Instant instant = Instant.parse("2024-01-31T09:00:00.00Z");
        long startDelay = 2000L;
        double accelerationFactor = 60d; // 1 sec / minute
        AbstractComponent.createComponent(ClocksServer.class.getCanonicalName(), new Object[]{
            CLOCK_URI,
            TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis() + startDelay),
            instant,
            accelerationFactor
        });
    }

    public void setupNode(ParsedData.Node parsedData) throws Exception {
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

        Object[] componentArgs = { nodeInfo, sensors };
        String nodeUri = AbstractComponent.createComponent(Node.class.getCanonicalName(), componentArgs);

        doPortConnection(
            nodeUri,
            Node.uri(Node.OUTBOUND_URI.REGISTRY, nodeInfo),
            Registry.INBOUND_URI.NODE.uri,
            ConnectorNodeRegistry.class.getCanonicalName()
        );
        sensorsAll.addAll(sensors);
        nodeInfosAll.add(nodeInfo);
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

        public interface CallbackI {

            void callback(String sensorId, SensorDataI sensorData);

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
