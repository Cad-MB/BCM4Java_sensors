package components;

import components.registry.RegistryPortFromNode;
import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;

import java.util.Set;

public class ConnectorNodeRegistry
        extends AbstractConnector
        implements RegistrationCI {
    @Override
    public boolean registered(String nodeIdentifier) throws Exception {
        return ((RegistryPortFromNode) this.offering).registered(nodeIdentifier);
    }

    @Override
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        return ((RegistryPortFromNode) this.offering).register(nodeInfo);
    }

    @Override
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction dir) throws Exception {
        return ((RegistryPortFromNode) this.offering).findNewNeighbour(nodeInfo, dir);
    }

    @Override
    public void unregister(String nodeIdentifier) throws Exception {
        ((RegistryPortFromNode) this.offering).unregister(nodeIdentifier);
    }
}
