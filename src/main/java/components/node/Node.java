package components.node;

import ast.query.Query;
import components.ConnectorNodeP2P;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.*;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import requests.ExecutionState;
import requests.NodeInfo;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

@OfferedInterfaces(offered={ NodeServicesCI.class, NodeP2PInCI.class })
@RequiredInterfaces(required={ RegistrationCI.class, NodeP2POutCI.class })
public class Node
    extends AbstractComponent
    implements SensorNodeP2PImplI {

    protected NodePortFromClient clientInboundPort;
    protected NodePortForRegistry registryOutboundPort;
    protected NodeInfo nodeInfo;

    protected NodePortForP2P portForP2P;
    protected NodePortFromP2P portFromP2P;

    protected Node(NodeInfo nodeInfo) throws Exception {
        super(1, 0);
        this.nodeInfo = nodeInfo;

        this.clientInboundPort = new NodePortFromClient(uri(INBOUND_URI.CLIENT, nodeInfo), this);
        this.clientInboundPort.publishPort();
        this.registryOutboundPort = new NodePortForRegistry(uri(OUTBOUND_URI.REGISTRY, nodeInfo), this);
        this.registryOutboundPort.publishPort();

        this.portForP2P = new NodePortForP2P(uri(OUTBOUND_URI.P2P), this);
        this.portForP2P.publishPort();
        this.portFromP2P = new NodePortFromP2P(uri(INBOUND_URI.P2P), this);
        this.portFromP2P.publishPort();

        this.nodeInfo.setEndPointInfo(new NodeInfo.EndPointInfo(uri(INBOUND_URI.CLIENT)));
        this.nodeInfo.setP2pEndPointInfo(new NodeInfo.EndPointInfo(uri(INBOUND_URI.P2P)));

        this.toggleLogging();
        this.toggleTracing();
        this.logMessage(this.nodeInfo.nodeIdentifier());
    }

    public static String uri(INBOUND_URI uri, NodeInfoI nodeInfo) {
        return uri.uri + "-" + nodeInfo.nodeIdentifier();
    }

    public static String uri(OUTBOUND_URI uri, NodeInfoI nodeInfo) {
        return uri.uri + "-" + nodeInfo.nodeIdentifier();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.clientInboundPort.unpublishPort();
            this.registryOutboundPort.unpublishPort();
            this.portFromP2P.unpublishPort();
            this.portForP2P.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        Set<NodeInfoI> neighbours = this.registryOutboundPort.register(this.nodeInfo);
        for (NodeInfoI neighbour : neighbours) {
            System.out.println("boucle " + nodeInfo.getNodeIdentifier() + " " + neighbour);
            ask4Connection(neighbour);
        }
        this.logMessage(this.nodeInfo.getNeighbours().toString());
    }

    @Override
    public synchronized void finalise() throws Exception {
        System.out.println(this.nodeInfo.getNodeIdentifier() + ": " + nodeInfo.getNeighbours().toString() + "\n");
        if (isPortConnected(uri(OUTBOUND_URI.REGISTRY))) {
            this.doPortDisconnection(uri(OUTBOUND_URI.REGISTRY));
        }
        if (isPortConnected(uri(OUTBOUND_URI.P2P))) {
            this.doPortDisconnection(uri(OUTBOUND_URI.P2P));
        }
        super.finalise();
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

    @Override
    public void ask4Connection(NodeInfoI neighbour) throws Exception {
        EndPointDescriptorI endPointInfo = neighbour.p2pEndPointInfo();
        assert endPointInfo instanceof NodeInfo.EndPointInfo;

        System.out.println(nodeInfo.nodeIdentifier() + ": " + endPointInfo);
        this.doPortConnection(
            uri(OUTBOUND_URI.P2P),
            endPointInfo.toString(),
            ConnectorNodeP2P.class.getCanonicalName()
        );
        portForP2P.ask4Connection(this.nodeInfo);
        this.nodeInfo.getNeighbours().add(neighbour);
        this.traceMessage("connected to " + neighbour + "\n");
    }

    public void disconnect(NodeInfoI neighbour) throws Exception {
        Direction dir = nodeInfo.getPosition().directionFrom(neighbour.nodePosition());
        nodeInfo.getNeighbours().remove(neighbour);
        this.registryOutboundPort.findNewNeighbour(nodeInfo, dir);
    }

    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        EndPointDescriptorI endPointInfo = neighbour.p2pEndPointInfo();
        assert endPointInfo instanceof NodeInfo.EndPointInfo;
        this.doPortConnection(
            uri(OUTBOUND_URI.P2P),
            endPointInfo.toString(),
            ConnectorNodeP2P.class.getCanonicalName()
        );
        portForP2P.ask4Disconnection(this.nodeInfo);

        final Set<NodeInfoI> neighbours = this.nodeInfo.getNeighbours();
        neighbours.remove(neighbour);
        this.nodeInfo.setNeighbours(neighbours);
    }

    public void connect(NodeInfoI neighbour) throws Exception {
        this.logMessage(this.nodeInfo.getNodeIdentifier() + " connection received from " + neighbour.nodeIdentifier());
        Set<NodeInfoI> neighbours = nodeInfo.getNeighbours();
        PositionI position = nodeInfo.getPosition();
        Optional<NodeInfoI> currentNeighbour =
            neighbours.stream()
                      .filter(currNeighbour ->
                                  currNeighbour.nodePosition().directionFrom(position) ==
                                  neighbour.nodePosition().directionFrom(position))
                      .findFirst();
        if (currentNeighbour.isPresent()) {
            NodeInfoI nodeInfoVoisin = currentNeighbour.get();
            ask4Disconnection(nodeInfoVoisin);
            neighbours.remove(nodeInfoVoisin);
        }
        neighbours.add(neighbour);
        nodeInfo.setNeighbours(neighbours);
    }

    public String uri(INBOUND_URI uri) {
        return uri.uri + "-" + nodeInfo.nodeIdentifier();
    }

    public String uri(OUTBOUND_URI uri) {
        return uri.uri + "-" + nodeInfo.nodeIdentifier();
    }

    public enum INBOUND_URI {
        CLIENT("node-inbound-client"),
        P2P("node-inbound-p2p"),

        P2P_NE("node-inbound-p2p-ne"),
        P2P_NW("node-inbound-p2p-nw"),
        P2P_SE("node-inbound-p2p-se"),
        P2P_SW("node-inbound-p2p-sw");

        public final String uri;

        INBOUND_URI(String uri) { this.uri = uri; }
    }

    public enum OUTBOUND_URI {
        REGISTRY("node-outbound-registry"),
        P2P("node-outbound-p2p-"),

        P2P_NE("node-outbound-p2p-ne"),
        P2P_NW("node-outbound-p2p-nw"),
        P2P_SE("node-outbound-p2p-se"),
        P2P_SW("node-outbound-p2p-sw");

        public final String uri;

        OUTBOUND_URI(String uri) { this.uri = uri; }
    }

    @Override
    public String toString() {
        return "Node{" +
               "nodeInfo=" + nodeInfo +
               '}';
    }

}

