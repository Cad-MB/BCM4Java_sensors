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

    // Constants for execution URI
    public static final String LOOKUP_EXEC_URI = "lookup_exec_uri";

    // Ports for registration and lookup services
    protected RegistryRegistrationInPort registrationInPort;
    protected RegistryLookupInPort lookupInPort;
    protected Map<String, NodeInfoI> registeredNodes;

    /**
     * Constructor for the Registry component.
     * Initializes the component with ports for registration and lookup and sets up the tracer window.
     *
     * @throws Exception If there is an issue initializing the component or the ports.
     */
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

    /**
     * Executes the main logic of the Registry component.
     * This method is typically used to manage operations once the component is fully deployed.
     *
     * @throws Exception If there is an error during execution.
     */
    @Override
    public void execute() throws Exception {
        super.execute();
        Thread.currentThread().setName("Registry");
        this.createNewExecutorService(LOOKUP_EXEC_URI, 3, true);
    }

    /**
     * Finds a node by its identifier.
     *
     * @param id The identifier of the node to find.
     * @return The connection information of the node if found, null otherwise.
     */
    public ConnectionInfoI findNodeById(String id) {
        return this.registeredNodes.get(id);
    }

    /**
     * Finds all nodes within a specified geographical zone.
     *
     * @param zone The geographical zone to search within.
     * @return A set of connection information for nodes within the specified zone.
     */
    public Set<ConnectionInfoI> findNodeByZone(GeographicalZoneI zone) {
        return registeredNodes
            .values()
            .stream()
            .filter(nodeInfo -> zone.in(nodeInfo.nodePosition()))
            .collect(Collectors.toSet());
    }

    /**
     * Registers a node and determines its neighbors based on spatial proximity.
     *
     * @param nodeInfo The information about the node being registered.
     * @return A set of node information for the neighbors of the registered node.
     */
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

    /**
     * Checks if a node is already registered.
     *
     * @param nodeIdentifier The identifier of the node to check.
     * @return True if the node is registered, false otherwise.
     */
    public synchronized Boolean isRegistered(String nodeIdentifier) {
        return registeredNodes.containsKey(nodeIdentifier);
    }

    /**
     * Finds the closest new neighbor in a given direction from the specified node.
     *
     * @param nodeInfo The node from which to find the neighbor.
     * @param dir The direction in which to look for the neighbor.
     * @return The information of the closest neighbor in the specified direction, null if no suitable neighbor is found.
     */
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

    /**
     * Finalizes the Registry component by ensuring all resources are cleanly released.
     *
     * @throws Exception If there is an issue during the finalization process.
     */
    @Override
    public synchronized void finalise() throws Exception {
        super.finalise();
    }

    /**
     * Unregisters a node by its identifier.
     *
     * @param nodeIdentifier The identifier of the node to unregister.
     * @return Void as an indication of the method completion.
     */
    public synchronized Void unregister(String nodeIdentifier) {
        registeredNodes.remove(nodeIdentifier);
        Visualisation.removeNodeInfo(nodeIdentifier);
        return null;
    }

    /**
     * Shuts down the Registry component.
     * This method ensures that all ports are unpublished and any other cleanup is performed before shutting down.
     *
     * @throws ComponentShutdownException If there is an issue during the shutdown process.
     */
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

    /**
     * Enumeration for inbound URI constants used by Registry.
     */
    public enum INBOUND_URI {
        REGISTRATION("registry-registration-inbound-uri"),
        LOOKUP("registry-lookup-inbound-uri");

        public final String uri;

        INBOUND_URI(String uri) {
            this.uri = uri;
        }
    }

}
