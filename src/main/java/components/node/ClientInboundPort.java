package components.node;

import ast.query.Query;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.ArrayList;

public class ClientInboundPort
        extends AbstractInboundPort
        implements NodeCI {
    public ClientInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, NodeCI.class, owner);
        assert owner instanceof Node;
    }

    @Override
    public ArrayList<String> evaluationB(Query q) throws Exception {
        return this.getOwner().handleRequest(owner -> ((Node) owner).evaluationB(q));
    }

    @Override
    public ArrayList<SensorDataI> evaluationG(Query q) throws Exception {
        return this.getOwner().handleRequest(owner -> ((Node) owner).evaluationG(q));
    }
}