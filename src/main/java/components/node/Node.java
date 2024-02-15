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
public class Node extends AbstractComponent implements SensorNodeP2PImplI {
    protected Node(NodeInfo nodeInfo) throws Exception {
        super(1, 0);
        this.nodeInfo = nodeInfo;

        this.clientInboundPort = new ClientInboundPort(INBOUND_URI.CLIENT.uri + nodeInfo.getNodeIdentifier(), this);
        this.clientInboundPort.publishPort();
        this.registryOutboundPort = new RegistryOutboundPort(OUTBOUND_URI.REGISTRY.uri + nodeInfo.getNodeIdentifier(), this);
        this.registryOutboundPort.publishPort();

        this.toggleLogging();
        this.toggleTracing();
    }

    @Override
    public void execute() throws Exception {
        Set<NodeInfoI> neighbours = this.registryOutboundPort.register(this.nodeInfo);
        this.nodeInfo.setNeighbours(neighbours);
        this.traceMessage(this.nodeInfo.getNeighbours().toString());
        super.execute();
    }

    protected ClientInboundPort clientInboundPort;
    protected RegistryOutboundPort registryOutboundPort;
    protected NodeInfo nodeInfo;

    @Override
    public synchronized void finalise() throws Exception {
        // this.registryOutboundPort.unregister(nodeInfo.nodeIdentifier());
        this.doPortDisconnection(OUTBOUND_URI.REGISTRY.uri + nodeInfo.getNodeIdentifier());
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.clientInboundPort.unpublishPort();
            this.registryOutboundPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    @Override
    public void ask4Connection(NodeInfoI neighbour) throws Exception {
        neighbour.p2pEndPointInfo();
    }

    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {

    }

    public ArrayList<String> evaluationB(Query q) throws Exception {
        return q.eval(new ExecutionState(nodeInfo)).positiveSensorNodes();
    }

    public ArrayList<SensorDataI> evaluationG(Query q) throws Exception {
        return q.eval(new ExecutionState(nodeInfo)).gatheredSensorsValues();
    }

    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        return null;
    }

    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {

    }

    public enum INBOUND_URI {
        CLIENT("node-inbound-client-");

        public final String uri;

        INBOUND_URI(String uri) {
            this.uri = uri;
        }
    }

    public enum OUTBOUND_URI {
        REGISTRY("node-outbound-registry");

        public final String uri;

        OUTBOUND_URI(String uri) {
            this.uri = uri;
        }
    }

}

