package components.registry.inbound_ports;

import components.registry.Registry;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;

import java.util.Set;

public class RegistryLookupInPort
    extends AbstractInboundPort
    implements LookupCI {

    public RegistryLookupInPort(String uri, ComponentI owner) throws Exception {
        super(uri, LookupCI.class, owner);
    }

    @Override
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
        return this.getOwner().handleRequest(Registry.LOOKUP_EXEC_URI, c -> ((Registry) c).findNodeById(sensorNodeId));
    }

    @Override
    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        return this.getOwner().handleRequest(Registry.LOOKUP_EXEC_URI, c -> ((Registry) c).findNodeByZone(z));
    }

}
