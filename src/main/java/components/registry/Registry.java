package components.registry;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;

import java.util.*;
import java.util.stream.Stream;

@OfferedInterfaces(offered = {RegistrationCI.class})
public class Registry extends AbstractComponent {
    protected Registry() throws Exception {
        super(1, 0);
        this.registeredNodes = new HashMap<>();
        this.clientInboundPort = new ClientInboundPort(INBOUND_URI.NODE.uri, this);
        this.clientInboundPort.publishPort();
    }

    protected ClientInboundPort clientInboundPort;
    HashMap<String, NodeInfoI> registeredNodes;

    public Set<NodeInfoI> register(NodeInfoI nodeInfo) {
        HashSet<NodeInfoI> neighbours = new HashSet<>();
        for (Direction dir : Direction.values()) {
            Optional<NodeInfoI> neighbour = findNewNeighbour(nodeInfo, dir);
            neighbour.ifPresent(neighbours::add);
        }
        registeredNodes.put(nodeInfo.nodeIdentifier(), nodeInfo);
        return neighbours;
    }

    public Boolean isRegistered(String nodeIdentifier) {
        return registeredNodes.containsKey(nodeIdentifier);
    }

    public Optional<NodeInfoI> findNewNeighbour(NodeInfoI nodeInfo, Direction dir) {
        PositionI targetPosition = nodeInfo.nodePosition();
        Stream<NodeInfoI> nodesInDirection = registeredNodes.values().stream().filter(rn -> rn.nodePosition().directionFrom(targetPosition) == dir);
        Optional<NodeInfoI> minDistanceNeighbour = nodesInDirection.min(Comparator.comparingDouble(n -> n.nodePosition().distance(targetPosition)));
        if (!minDistanceNeighbour.isPresent() ||
            minDistanceNeighbour.get().nodePosition().distance(nodeInfo.nodePosition()) > nodeInfo.nodeRange()) {
            return Optional.empty();
        }
        return minDistanceNeighbour;
    }

    @Override
    public synchronized void finalise() throws Exception {
        super.finalise();
    }

    public Void unregister(String nodeIdentifier) {
        registeredNodes.remove(nodeIdentifier);
        return null;
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.clientInboundPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    public enum INBOUND_URI {
        NODE("registry-node-inbound-uri");

        public final String uri;

        INBOUND_URI(String uri) {
            this.uri = uri;
        }
    }
}
