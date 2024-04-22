package components.registry;

import components.registry.inbound_ports.RegistryLookupInPort;
import components.registry.inbound_ports.RegistryRegistrationInPort;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OfferedInterfaces(offered={ RegistrationCI.class, LookupCI.class })
public class Registry
    extends AbstractComponent {

    public static final String LOOKUP_EXEC_URI = "lookup_exec_uri";

    // todo ajouter une interface offerte pour les ports
    protected RegistryRegistrationInPort registrationInPort;
    protected RegistryLookupInPort lookupInPort;
    protected Map<String, NodeInfoI> registeredNodes;

    protected Registry() throws Exception {
        super(8, 8);
        this.registeredNodes = new ConcurrentHashMap<>();
        this.registrationInPort = new RegistryRegistrationInPort(INBOUND_URI.REGISTRATION.uri, this);
        this.registrationInPort.publishPort();

        this.lookupInPort = new RegistryLookupInPort(INBOUND_URI.LOOKUP.uri, this);
        this.lookupInPort.publishPort();

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
        this.logMessage("Registry");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        Thread.currentThread().setName("Registry");
        this.createNewExecutorService(LOOKUP_EXEC_URI, 3, true);
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

    public synchronized Boolean isRegistered(String nodeIdentifier) {
        return registeredNodes.containsKey(nodeIdentifier);
    }

    public synchronized NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction dir) {
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
            this.registrationInPort.unpublishPort();
            this.lookupInPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    public enum INBOUND_URI {
        REGISTRATION("registry-registration-inbound-uri"),
        LOOKUP("registry-lookup-inbound-uri");

        public final String uri;

        INBOUND_URI(String uri) {
            this.uri = uri;
        }
    }

}
