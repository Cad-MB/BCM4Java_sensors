package components.registry;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@OfferedInterfaces(offered = {RegistrationCI.class})
public class Registry extends AbstractComponent {

    public static final String CLIENT_INBOUND_PORT_URI = "registry-node-inbound-uri";

    protected ClientInboundPort clientInboundPort;
    HashMap<String, NodeInfoI> registeredNodes;

    protected Registry() throws Exception {
        super(1, 0);
        this.registeredNodes = new HashMap<>();
        clientInboundPort = new ClientInboundPort(CLIENT_INBOUND_PORT_URI, this);
        clientInboundPort.publishPort();
    }

    public Boolean isRegistered(String nodeIdentifier) {
        return registeredNodes.containsKey(nodeIdentifier);
    }

    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        registeredNodes.put(nodeInfo.nodeIdentifier(), nodeInfo);
        return new HashSet<NodeInfoI>() {{
            findNewNeighbour(nodeInfo, Direction.NE);
            findNewNeighbour(nodeInfo, Direction.NW);
            findNewNeighbour(nodeInfo, Direction.SE);
            findNewNeighbour(nodeInfo, Direction.SW);
        }};
    }

    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction dir) throws Exception {
        PositionI targetPosition = nodeInfo.nodePosition();
        Stream<NodeInfoI> registerdNodesStream = registeredNodes.values().stream();
        Stream<NodeInfoI> nodesInDirection = null;
        switch (dir) {
            case NE:
                nodesInDirection = registerdNodesStream.filter(rn -> rn.nodePosition().eastOf(targetPosition) && rn.nodePosition().northOf(targetPosition));
                break;
            case NW:
                nodesInDirection = registerdNodesStream.filter(rn -> rn.nodePosition().westOf(targetPosition) && rn.nodePosition().northOf(targetPosition));
                break;
            case SE:
                nodesInDirection = registerdNodesStream.filter(rn -> rn.nodePosition().eastOf(targetPosition) && rn.nodePosition().southOf(targetPosition));
                break;
            case SW:
                nodesInDirection = registerdNodesStream.filter(rn -> rn.nodePosition().westOf(targetPosition) && rn.nodePosition().southOf(targetPosition));
                break;
        }
        return nodesInDirection.min(Comparator.comparingDouble(n -> n.nodePosition().distance(targetPosition))).get();
    }

    public Void unregister(String nodeIdentifier) {
        registeredNodes.remove(nodeIdentifier);
        return null;
    }
}
