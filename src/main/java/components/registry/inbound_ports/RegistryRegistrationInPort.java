package components.registry.inbound_ports;

import components.registry.Registry;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;

import java.util.Set;

public class RegistryRegistrationInPort
    extends AbstractInboundPort
    implements RegistrationCI {

    public RegistryRegistrationInPort(String uri, ComponentI owner) throws Exception {
        super(uri, RegistrationCI.class, owner);
    }

    @Override
    public boolean registered(String nodeIdentifier) throws Exception {
        return this.getOwner().handleRequest(c -> ((Registry) c).isRegistered(nodeIdentifier));
    }

    @Override
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        return this.getOwner().handleRequest(c -> ((Registry) c).register(nodeInfo));
    }

    @Override
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction dir) throws Exception {
        return this.getOwner().handleRequest(c -> ((Registry) c).findNewNeighbour(nodeInfo, dir));
    }

    @Override
    public void unregister(String nodeIdentifier) throws Exception {
        this.getOwner().handleRequest(c -> ((Registry) c).unregister(nodeIdentifier));
    }

}
