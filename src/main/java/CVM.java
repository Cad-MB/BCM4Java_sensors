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
import java.util.HashSet;
import java.util.Set;

public class CVM
    extends AbstractCVM {

    public CVM() throws Exception {
    }

    public static void main(String[] args) throws Exception {
        CVM c = new CVM();
        c.startStandardLifeCycle(10000L);
        System.exit(0);
    }

    private void test1() throws Exception {
        super.deploy();
        NodeInfo nodeInfo1 = new NodeInfo(100, "node1", new Position(1d, 2d)); // Initialize currentNodeInfo
        Set<SensorDataI> sensors1 = new HashSet<>();
        sensors1.add(new SensorData<>(nodeInfo1.nodeIdentifier(), "sensor1", 100d, Instant.now()));
        NodeInfo nodeInfo2 = new NodeInfo(200, "node2", new Position(2d, 3d));
        Set<SensorDataI> sensors2 = new HashSet<>();
        sensors2.add(new SensorData<>(nodeInfo2.nodeIdentifier(), "sensor1", 150d, Instant.now()));

        String clientURI = AbstractComponent.createComponent(Client.class.getCanonicalName(), new Object[]{});
        AbstractComponent.createComponent(Registry.class.getCanonicalName(), new Object[]{});
        String node1URI = AbstractComponent.createComponent(Node.class.getCanonicalName(), new Object[]{ nodeInfo1 });
        String node2URI = AbstractComponent.createComponent(Node.class.getCanonicalName(), new Object[]{ nodeInfo2 });

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

        // this.doPortConnection(
        //     clientURI,
        //     Client.OUTBOUND_URI.NODE.uri,
        //     Node.INBOUND_URI.CLIENT.uri + nodeInfo1.nodeIdentifier(),
        //     ConnectorClientNode.class.getCanonicalName()
        // );

        Thread.sleep(2000);
        this.doPortConnection(
            clientURI,
            Client.OUTBOUND_URI.REGISTRY.uri,
            Registry.INBOUND_URI.CLIENT.uri,
            ConnectorClientRegistry.class.getCanonicalName()
        );

    }

    public void test2() throws Exception {
        super.deploy();
        NodeInfo nodeInfo1 = new NodeInfo(100, "node1", new Position(1d, 2d)); // Initialize currentNodeInfo
        Set<SensorDataI> sensors = new HashSet<>();
        sensors.add(new SensorData<>(nodeInfo1.nodeIdentifier(), "sensor1", 100d, Instant.now()));

        String node1URI = AbstractComponent.createComponent(Node.class.getCanonicalName(), new Object[]{ nodeInfo1 });
        AbstractComponent.createComponent(Registry.class.getCanonicalName(), new Object[]{});
        String clientURI = AbstractComponent.createComponent(Client.class.getCanonicalName(), new Object[]{});


        this.doPortConnection(
            node1URI,
            Node.OUTBOUND_URI.REGISTRY.uri + nodeInfo1.nodeIdentifier(),
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
        test1();
    }

}
