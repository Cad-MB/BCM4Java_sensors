package components.node;

import ast.query.Query;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import requests.ExecutionState;
import requests.NodeInfo;

import java.util.ArrayList;
import java.util.Set;

@OfferedInterfaces(offered = {NodeCI.class})
@RequiredInterfaces(required = {RegistrationCI.class})
public class Node extends AbstractComponent implements SensorNodeP2PImplI {

    protected NodePortFromClient clientInboundPort;
    protected NodePortForRegistry registryOutboundPort;
    protected NodeInfo nodeInfo;

    protected Node(NodeInfo nodeInfo) throws Exception {
        super(1, 0);
        this.nodeInfo = nodeInfo;

        this.clientInboundPort = new NodePortFromClient(INBOUND_URI.CLIENT.uri + nodeInfo.getNodeIdentifier(), this);
        this.clientInboundPort.publishPort();
        this.registryOutboundPort = new NodePortForRegistry(OUTBOUND_URI.REGISTRY.uri + nodeInfo.getNodeIdentifier(), this);
        this.registryOutboundPort.publishPort();
        this.nodeInfo.setEndPointInfo(clientInboundPort);

        this.toggleLogging();
        this.toggleTracing();
    }

    @Override
    public void execute() throws Exception {
        Set<NodeInfoI> neighbours = this.registryOutboundPort.register(this.nodeInfo);
        this.nodeInfo.setNeighbours(neighbours);
        for (NodeInfoI neighbour : neighbours) {
            ask4Connection(neighbour);
        }

        this.traceMessage(this.nodeInfo.getNeighbours().toString());
        super.execute();
    }

    @Override
    public synchronized void finalise() throws Exception {
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
        // EndPointDescriptorI voisin = neighbour.p2pEndPointInfo();
        // assert voisin instanceof NodeInfo;
        // Set<NodeInfoI> neighbours = ((NodeInfo) voisin).getNeighbours();
        // List<NodeInfoI> ourDirectionNeighbour = neighbours.stream().filter(n -> n.nodePosition().directionFrom(this.nodeInfo.getPosition()) == n.nodePosition().directionFrom(((NodeInfo) voisin).getPosition())).collect(Collectors.toList());
        // if (ourDirectionNeighbour.isEmpty()) {
        //     neighbours.add(this.nodeInfo);
        // } else {
        //     neighbours.remove(ourDirectionNeighbour.get(0));
        //     neighbours.add(this.nodeInfo);
        // }
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

