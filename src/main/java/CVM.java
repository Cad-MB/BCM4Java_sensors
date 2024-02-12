import components.ConnectorClientNode;
import components.ConnectorNodeRegistry;
import components.client.Client;
import components.node.Node;
import components.registry.Registry;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;

public class CVM
        extends AbstractCVM {
    protected NodeInfo nodeInfo;

    public CVM() throws Exception {
    }

    @Override
    public void deploy() throws Exception {
        this.nodeInfo = new NodeInfo(100, "node1"); // Initialize currentNodeInfo
        this.nodeInfo.sensors = new HashMap<>();
        this.nodeInfo.sensors.put("sensor1", new SensorData<>(this.nodeInfo.getNodeIdentifier(), "sensor1", 100d, Instant.now()));

        String cURI = AbstractComponent.createComponent(Client.class.getCanonicalName(), new Object[]{});
        String registryURI = AbstractComponent.createComponent(Registry.class.getCanonicalName(), new Object[]{});
        String nodeURI = AbstractComponent.createComponent(Node.class.getCanonicalName(), new Object[]{nodeInfo});
        this.doPortConnection(nodeURI, Node.REGISTRY_OUTBOUND_PORT_URI, Registry.CLIENT_INBOUND_PORT_URI, ConnectorNodeRegistry.class.getCanonicalName());
        this.doPortConnection(cURI, Client.COP_URI, Node.NNIP_URI, ConnectorClientNode.class.getCanonicalName());

        super.deploy();
    }

    public static void main(String[] args) throws Exception {
        CVM c = new CVM();
        c.startStandardLifeCycle(10000L);
        System.exit(0);
    }
}
