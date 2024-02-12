package components.node;

import ast.query.Query;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import requests.ExecutionState;
import requests.NodeInfo;
import requests.SensorData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

@OfferedInterfaces(offered = {NodeCI.class})
@RequiredInterfaces(required = {RegistrationCI.class})
public class Node extends AbstractComponent {

    public static final String NNIP_URI = "nnip-uri";

    public static final String REGISTRY_OUTBOUND_PORT_URI = "node-registry-uri";
    protected ClientInboundPort clientInboundPort;
    protected RegistryOutboundPort registryOutboundPort;
    protected NodeInfo nodeInfo;

    protected Node(NodeInfo nodeInfo) throws Exception {
        super(1, 0);
        this.nodeInfo = nodeInfo;
        this.clientInboundPort = new ClientInboundPort(NNIP_URI, this);
        this.clientInboundPort.publishPort();
        this.registryOutboundPort = new RegistryOutboundPort(REGISTRY_OUTBOUND_PORT_URI, this);
        this.registryOutboundPort.publishPort();
    }

    @Override
    public void execute() throws Exception {
        Set<NodeInfoI> neighbours = this.registryOutboundPort.register(this.nodeInfo);
        this.nodeInfo.setNeighbours(neighbours);
        super.execute();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.clientInboundPort.unpublishPort();
            this.registryOutboundPort.unpublishPort();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.shutdown();
    }

    public ArrayList<String> evaluationB(Query q) throws Exception {
        return q.eval(new ExecutionState(nodeInfo)).positiveSensorNodes();
    }

    public ArrayList<SensorDataI> evaluationG(Query q) throws Exception {
        return q.eval(new ExecutionState(nodeInfo)).gatheredSensorsValues();
    }

}

