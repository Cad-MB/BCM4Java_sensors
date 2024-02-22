import components.ConnectorClientNode;
import components.ConnectorClientRegistry;
import components.ConnectorNodeRegistry;
import components.client.Client;
import components.node.Node;
import components.registry.Registry;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import requests.NodeInfo;
import requests.Position;
import requests.SensorData;

import java.time.Instant;
import java.util.HashMap;

public class CVM
    extends AbstractCVM {

    public CVM() throws Exception {
    }

    private void test1() throws Exception {
        NodeInfo nodeInfo1 = new NodeInfo(100, "node1", new Position(1d, 2d)); // Initialize currentNodeInfo
        nodeInfo1.sensors = new HashMap<>();
        nodeInfo1.sensors.put(
            "sensor1", new SensorData<>(nodeInfo1.getNodeIdentifier(), "sensor1", 100d, Instant.now())
        );
        NodeInfo nodeInfo2 = new NodeInfo(200, "node2", new Position(2d, 3d));
        nodeInfo2.sensors = new HashMap<>();
        nodeInfo2.sensors.put(
            "sensor1", new SensorData<>(nodeInfo2.getNodeIdentifier(), "sensor1", 150d, Instant.now())
        );

        String clientURI = AbstractComponent.createComponent(Client.class.getCanonicalName(), new Object[]{});
        String registryURI = AbstractComponent.createComponent(Registry.class.getCanonicalName(), new Object[]{});
        String node1URI = AbstractComponent.createComponent(Node.class.getCanonicalName(), new Object[]{nodeInfo1});
        String node2URI = AbstractComponent.createComponent(Node.class.getCanonicalName(), new Object[]{nodeInfo2});

        this.doPortConnection(
            node1URI,
            Node.OUTBOUND_URI.REGISTRY.uri + nodeInfo1.nodeIdentifier(),
            Registry.INBOUND_URI.NODE.uri,
            ConnectorNodeRegistry.class.getCanonicalName()
        );

        this.doPortConnection(
            node2URI,
            Node.OUTBOUND_URI.REGISTRY.uri + nodeInfo2.getNodeIdentifier(),
            Registry.INBOUND_URI.NODE.uri,
            ConnectorNodeRegistry.class.getCanonicalName()
        );

        this.doPortConnection(
            clientURI,
            Client.OUTBOUND_URI.NODE.uri,
            Node.INBOUND_URI.CLIENT.uri + nodeInfo1.nodeIdentifier(),
            ConnectorClientNode.class.getCanonicalName()
        );

    }

    public void test2() throws Exception {
        NodeInfo nodeInfo1 = new NodeInfo(100, "node1", new Position(1d, 2d)); // Initialize currentNodeInfo
        nodeInfo1.sensors = new HashMap<>();
        nodeInfo1.sensors.put(
            "sensor1", new SensorData<>(nodeInfo1.getNodeIdentifier(), "sensor1", 100d, Instant.now())
        );

        AbstractComponent.createComponent(Registry.class.getCanonicalName(), new Object[]{});
        String node1URI = AbstractComponent.createComponent(Node.class.getCanonicalName(), new Object[]{nodeInfo1});
        String clientURI = AbstractComponent.createComponent(Client.class.getCanonicalName(), new Object[]{});

        this.doPortConnection(
            node1URI,
            Node.OUTBOUND_URI.REGISTRY.uri + nodeInfo1.nodeIdentifier(),
            Registry.INBOUND_URI.NODE.uri,
            ConnectorNodeRegistry.class.getCanonicalName()
        );

        // Thread.sleep(2000);
        this.doPortConnection(
            clientURI,
            Client.OUTBOUND_URI.REGISTRY.uri,
            Registry.INBOUND_URI.CLIENT.uri,
            ConnectorClientRegistry.class.getCanonicalName()
        );
    }

    @Override
    public void deploy() throws Exception {
        test2();
        super.deploy();
    }

    public static void main(String[] args) throws Exception {
        CVM c = new CVM();
        c.startStandardLifeCycle(10000L);
        System.exit(0);
    }
}
