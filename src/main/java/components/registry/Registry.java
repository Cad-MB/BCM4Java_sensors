package components.registry;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.*;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import logger.CustomTraceWindow;
import visualization.Visualisation;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OfferedInterfaces(offered={ RegistrationCI.class, LookupCI.class })
public class Registry
    extends AbstractComponent {

    // todo ajouter une interface offerte pour les ports
    protected RegistryPortFromNode registryPortForNode;
    protected RegistryPortFromClient registryPortFromClient;
    protected HashMap<String, NodeInfoI> registeredNodes;

    protected Registry() throws Exception {
        super(1, 1);
        this.registeredNodes = new HashMap<>();
        this.registryPortForNode = new RegistryPortFromNode(INBOUND_URI.NODE.uri, this);
        this.registryPortForNode.publishPort();

        this.registryPortFromClient = new RegistryPortFromClient(INBOUND_URI.CLIENT.uri, this);
        this.registryPortFromClient.publishPort();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        CustomTraceWindow tracerWindow = new CustomTraceWindow(
            "Registry",
            0, 0,
            screenSize.width, screenSize.height / 5,
            0, 3
        );
        tracerWindow.setBackgroundColor(Color.decode("#1976D2"));
        tracerWindow.setForegroundColor(Color.WHITE);
        setTracer(tracerWindow);

        this.toggleTracing();
        this.toggleLogging();
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        Thread.currentThread().setName("Registry");
    }

    public ConnectionInfoI findNodeById(String id) {
        return this.registeredNodes.get(id);
    }

    public Set<ConnectionInfoI> findNodeByZone(GeographicalZoneI zone) {
        return registeredNodes
            .values()
            .stream()
            .filter(nodeInfo -> zone.in(nodeInfo.nodePosition()))
            .collect(Collectors.toSet());
    }

    public synchronized Set<NodeInfoI> register(NodeInfoI nodeInfo) {
        HashSet<NodeInfoI> neighbours = new HashSet<>();
        for (Direction dir : Direction.values()) {
            NodeInfoI neighbour = findNewNeighbour(nodeInfo, dir);
            if (neighbour != null) neighbours.add(neighbour);
        }
        registeredNodes.put(nodeInfo.nodeIdentifier(), nodeInfo);
        logMessage("registered: " + nodeInfo);
        Visualisation.addNodeInfo(nodeInfo.nodeIdentifier(), nodeInfo);
        return neighbours;
    }

    public Boolean isRegistered(String nodeIdentifier) {
        return registeredNodes.containsKey(nodeIdentifier);
    }

    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction dir) {
        PositionI targetPosition = nodeInfo.nodePosition();
        Stream<NodeInfoI> nodesInDirection = registeredNodes.values().stream().filter(n -> targetPosition.directionFrom(n.nodePosition()) == dir);
        Optional<NodeInfoI> closestNeighbour = nodesInDirection.min(Comparator.comparingDouble(n -> n.nodePosition().distance(targetPosition)));
        if (closestNeighbour.isPresent()) {
            NodeInfoI cn = closestNeighbour.get();
            double distance = cn.nodePosition().distance(nodeInfo.nodePosition());
            if (cn.equals(nodeInfo) || cn.nodeRange() < distance || nodeInfo.nodeRange() < distance) {
                return null;
            }
        }
        return closestNeighbour.orElse(null);
    }

    @Override
    public synchronized void finalise() throws Exception {
        super.finalise();
    }

    public synchronized Void unregister(String nodeIdentifier) {
        registeredNodes.remove(nodeIdentifier);
        Visualisation.removeNodeInfo(nodeIdentifier);
        return null;
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.registryPortForNode.unpublishPort();
            this.registryPortFromClient.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    public enum INBOUND_URI {
        NODE("registry-node-inbound-uri"),
        CLIENT("registry-from-client-uri");

        public final String uri;

        INBOUND_URI(String uri) {
            this.uri = uri;
        }
    }

}
