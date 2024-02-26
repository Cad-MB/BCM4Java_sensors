package components.registry;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;

import java.util.Set;

public class RegistryPortFromClient
    extends AbstractInboundPort
    implements LookupCI {

    public RegistryPortFromClient(String uri, ComponentI owner) throws Exception {
        super(uri, LookupCI.class, owner);
        assert owner instanceof Registry;
    }

    @Override
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
        return this.getOwner().handleRequest(c -> ((Registry) c).findNodeById(sensorNodeId));
    }

    @Override
    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        return this.getOwner().handleRequest(c -> ((Registry) c).findNodeByZone(z));
    }

}
