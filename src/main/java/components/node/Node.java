/**
 * This class represents a sensor node in the sensor network system.
 * It communicates with the registry for registration and with other nodes for peer-to-peer communication.
 * The node processes queries and executes them either locally or forwards them to neighboring nodes.
 */
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
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import requests.ExecutionState;
import requests.NodeInfo;
import requests.ProcessingNode;
import requests.RequestContinuation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@OfferedInterfaces(offered={ NodeClientInCI.class, NodeP2PInCI.class })
@RequiredInterfaces(required={ RegistrationCI.class, NodeP2POutCI.class })
public class Node
    extends AbstractComponent
    implements SensorNodeP2PImplI {

    protected NodePortFromClient clientInboundPort;
    protected NodePortForRegistry registryOutboundPort;
    protected NodeInfo nodeInfo;
    protected ProcessingNodeI processingNode;

    protected HashMap<Direction, NodePortForP2P> portsForP2P = new HashMap<>();
    protected NodePortFromP2P portFromP2P;

    /**
     * Constructs a new sensor node with the given node information and sensor data.
     * Initializes ports for communication and toggles logging and tracing.
     *
     * @param nodeInfo   the information of the sensor node
     * @param sensorData the sensor data collected by the node
     * @throws Exception if an error occurs during initialization
     */
    protected Node(NodeInfo nodeInfo, Set<SensorDataI> sensorData) throws Exception {
        super(1, 0);
        this.nodeInfo = nodeInfo;
        this.processingNode = new ProcessingNode(
            nodeInfo.nodeIdentifier(),
            nodeInfo.nodePosition(),
            new HashSet<>(),
            sensorData
        );

        this.clientInboundPort = new NodePortFromClient(uri(INBOUND_URI.CLIENT, nodeInfo), this);
        this.clientInboundPort.publishPort();

        this.registryOutboundPort = new NodePortForRegistry(uri(OUTBOUND_URI.REGISTRY, nodeInfo), this);
        this.registryOutboundPort.publishPort();

        for (Direction dir : Direction.values()) {
            NodePortForP2P port = new NodePortForP2P(OUTBOUND_URI.P2P(dir, nodeInfo), this);
            port.publishPort();
            portsForP2P.put(dir, port);
        }

        this.portFromP2P = new NodePortFromP2P(uri(INBOUND_URI.P2P), this);
        this.portFromP2P.publishPort();

        this.nodeInfo.setEndPointInfo(new NodeInfo.EndPointInfo(uri(INBOUND_URI.CLIENT)));
        this.nodeInfo.setP2pEndPointInfo(new NodeInfo.EndPointInfo(uri(INBOUND_URI.P2P)));

        this.toggleLogging();
        this.toggleTracing();
        this.logMessage(this.nodeInfo.nodeIdentifier());
    }


    /**
     * Executes the sensor node.
     * Registers with the registry and establishes connections with neighboring nodes.
     * @throws Exception if an error occurs during execution
     */
    @Override
    public void execute() throws Exception {
        super.execute();
        Set<NodeInfoI> neighbours = this.registryOutboundPort.register(this.nodeInfo);
        for (NodeInfoI neighbour : neighbours) {
            ask4Connection(neighbour);
        }
        this.logMessage(this.processingNode.getNeighbours().toString());
        System.out.println(nodeInfo.nodeIdentifier() + ": neighbours = " + processingNode.getNeighbours());
    }

    /**
     * Shuts down the sensor node.
     * Unpublishes ports and shuts down gracefully.
     * @throws ComponentShutdownException if an error occurs during shutdown
     */
    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.clientInboundPort.unpublishPort();
            this.registryOutboundPort.unpublishPort();
            this.portFromP2P.unpublishPort();
            for (NodePortForP2P port : this.portsForP2P.values()) {
                port.unpublishPort();
            }
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    /**
     * Finalizes the sensor node.
     * Disconnects from ports and performs necessary cleanup.
     * @throws Exception if an error occurs during finalization
     */
    @Override
    public synchronized void finalise() throws Exception {
        if (isPortConnected(uri(OUTBOUND_URI.REGISTRY))) {
            this.doPortDisconnection(uri(OUTBOUND_URI.REGISTRY));
        }
        for (Direction dir : Direction.values()) {
            if (isPortConnected(OUTBOUND_URI.P2P(dir, nodeInfo))) {
                this.doPortDisconnection(OUTBOUND_URI.P2P(dir, nodeInfo));
            }
        }
        super.finalise();
    }

    /**
     * Executes a request on the sensor node.
     * Processes the request locally or forwards it to neighboring nodes based on the query evaluation.
     * @param request the request to be executed
     * @return the result of the query execution
     * @throws Exception if an error occurs during execution
     */
    public QueryResultI execute(RequestI request) throws Exception {
        assert request.getQueryCode() instanceof Query;
        Query query = (Query) request.getQueryCode();
        ExecutionState executionState = new ExecutionState(processingNode);
        QueryResultI evaled = query.eval(executionState);
        System.out.println("request");
        if (executionState.isDirectional() && executionState.noMoreHops()) {
            return evaled;
        }
        if (executionState.isFlooding()) {
            for (NodeInfoI neighbourInfo: processingNode.getNeighbours()) {
                System.out.println("neighbourInfo = " + neighbourInfo);
                if(!executionState.isNodeAlreadyDone(neighbourInfo.nodeIdentifier()) &&
                   executionState.withinMaximalDistance(neighbourInfo.nodePosition())) {

                    Direction dir = nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition());
                    // todo remove rbase from execution
                    RequestContinuation requestContinuation = new RequestContinuation(request, executionState);
                    QueryResultI contRes = portsForP2P.get(dir).execute(requestContinuation);

                    evaled.gatheredSensorsValues().addAll(contRes.gatheredSensorsValues());
                    evaled.positiveSensorNodes().addAll(contRes.positiveSensorNodes());
                }
            }
        }
        for (Direction dir : executionState.getDirections()) {
            // System.out.println(
            //     nodeInfo.nodeIdentifier() + ": " + dir + "" " + isPortConnected(OUTBOUND_URI.P2P(dir, nodeInfo)));

            if (isPortConnected(OUTBOUND_URI.P2P(dir, nodeInfo))) {
                RequestContinuation requestContinuation = new RequestContinuation(request, executionState);
                // todo : mettre a jour execState pour enlever les autres directions
                QueryResultI contRes = portsForP2P.get(dir).execute(requestContinuation);
                evaled.gatheredSensorsValues().addAll(contRes.gatheredSensorsValues());
                evaled.positiveSensorNodes().addAll(contRes.positiveSensorNodes());
            }
        }
        return evaled;
    }

    /**
     * Executes a request asynchronously.
     * @param requestContinuation the continuation of the request to execute
     * @throws Exception if an error occurs during execution
     */
    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {

    }

    /**
     * Executes a request asynchronously.
     * @param requestI the request to execute
     * @throws Exception if an error occurs during execution
     */
    public void executeAsync(RequestI requestI) throws Exception {

    }

    /**
     * Executes a continuation of a request.
     * Processes the continuation and returns the query result.
     * @param request the continuation of the request to execute
     * @return the query result
     * @throws Exception if an error occurs during execution
     */
    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        System.out.println("called execute cont");
        ExecutionStateI execState = request.getExecutionState();
        execState.incrementHops();
        execState.updateProcessingNode(this.processingNode);
        assert execState instanceof ExecutionState;

        QueryResultI evaled = ((Query) request.getQueryCode()).eval(execState);
        if (execState.isDirectional()) {
            for (Direction dir : execState.getDirections()) {
                if (isPortConnected(OUTBOUND_URI.P2P(dir, nodeInfo))) {
                    QueryResultI contEvaled = portsForP2P.get(dir).execute(request);
                    evaled.gatheredSensorsValues().addAll(contEvaled.gatheredSensorsValues());
                    evaled.positiveSensorNodes().addAll(contEvaled.positiveSensorNodes());
                }
            }
        } else if (execState.isFlooding()) {
            for (NodeInfoI neighbourInfo: processingNode.getNeighbours()) {
                if(!((ExecutionState) execState).isNodeAlreadyDone(neighbourInfo.nodeIdentifier()) &&
                   execState.withinMaximalDistance(neighbourInfo.nodePosition())) {

                    Direction dir = nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition());
                    RequestContinuation requestContinuation = new RequestContinuation(request, execState);
                    QueryResultI contRes = portsForP2P.get(dir).execute(requestContinuation);

                    evaled.gatheredSensorsValues().addAll(contRes.gatheredSensorsValues());
                    evaled.positiveSensorNodes().addAll(contRes.positiveSensorNodes());
                }
            }
        }
        return evaled;
    }

    /**
     * Requests a connection to a neighboring node.
     * @param neighbour the neighboring node to connect to
     * @throws Exception if an error occurs during the connection process
     */
    @Override
    public void ask4Connection(NodeInfoI neighbour) throws Exception {
        EndPointDescriptorI endPointInfo = neighbour.p2pEndPointInfo();
        assert endPointInfo instanceof NodeInfo.EndPointInfo;

        Direction dir = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
        this.doPortConnection(
            OUTBOUND_URI.P2P(dir, nodeInfo),
            endPointInfo.toString(),
            ConnectorNodeP2P.class.getCanonicalName()
        );
        portsForP2P.get(dir).ask4Connection(this.nodeInfo);
        this.processingNode.getNeighbours().add(neighbour);
        this.traceMessage("connected to " + neighbour + "\n");
    }

    /**
     * Requests disconnection from a neighboring node.
     * @param neighbour the neighboring node to disconnect from
     * @throws Exception if an error occurs during the disconnection process
     */
    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        EndPointDescriptorI endPointInfo = neighbour.p2pEndPointInfo();
        assert endPointInfo instanceof NodeInfo.EndPointInfo;
        Direction dir = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
        portsForP2P.get(dir).ask4Disconnection(this.nodeInfo);
        this.doPortDisconnection(OUTBOUND_URI.P2P(dir, nodeInfo));

        Set<NodeInfoI> neighbours = this.processingNode.getNeighbours();
        neighbours.remove(neighbour);
    }

    /**
     * Connects to a neighboring node.
     * @param neighbour the neighboring node to connect to
     * @throws Exception if an error occurs during the connection process
     */
    public void connect(NodeInfoI neighbour) throws Exception {
        this.logMessage(this.nodeInfo.nodeIdentifier() + " connection received from " + neighbour.nodeIdentifier());
        Set<NodeInfoI> neighbours = processingNode.getNeighbours();
        PositionI position = nodeInfo.nodePosition();
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
        this.doPortConnection(
            OUTBOUND_URI.P2P(nodeInfo.nodePosition().directionFrom(neighbour.nodePosition()), nodeInfo),
            neighbour.p2pEndPointInfo().toString(),
            ConnectorNodeP2P.class.getCanonicalName());
        neighbours.add(neighbour);
    }

    /**
     * Disconnects from a neighboring node.
     * @param neighbour the neighboring node to disconnect from
     * @throws Exception if an error occurs during the disconnection process
     */
    public void disconnect(NodeInfoI neighbour) throws Exception {
        Direction dir = nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
        processingNode.getNeighbours().remove(neighbour);
        this.doPortDisconnection(OUTBOUND_URI.P2P(dir, nodeInfo));
        NodeInfoI newNeighbour = this.registryOutboundPort.findNewNeighbour(nodeInfo, dir);
        if (newNeighbour != null) {
            ask4Connection(newNeighbour);
        }
    }

    // Static methods to generate URIs for inbound and outbound ports

    public static String uri(INBOUND_URI uri, NodeInfoI nodeInfo) {
        return uri.uri + nodeInfo.nodeIdentifier();
    }

    public static String uri(OUTBOUND_URI uri, NodeInfoI nodeInfo) {
        return uri.uri + nodeInfo.nodeIdentifier();
    }

    // Methods to generate URIs for inbound and outbound ports

    public String uri(INBOUND_URI uri) {
        return uri.uri + nodeInfo.nodeIdentifier();
    }

    public String uri(OUTBOUND_URI uri) {
        return uri.uri + nodeInfo.nodeIdentifier();
    }

    // Enumeration of inbound port URIs

    public enum INBOUND_URI {
        CLIENT("node-inbound-client-"),
        P2P("node-inbound-p2p-");

        public final String uri;

        INBOUND_URI(String uri) { this.uri = uri; }
    }

    // Enumeration of outbound port URIs

    public enum OUTBOUND_URI {
        REGISTRY("node-outbound-registry-");

        public final String uri;

        static String P2P(Direction dir, NodeInfoI nodeInfo) {
            switch (dir) {
                case NE:
                    return "node-outbound-p2p-ne-" + nodeInfo.nodeIdentifier();
                case NW:
                    return "node-outbound-p2p-nw-" + nodeInfo.nodeIdentifier();
                case SE:
                    return "node-outbound-p2p-se-" + nodeInfo.nodeIdentifier();
                case SW:
                    return "node-outbound-p2p-sw-" + nodeInfo.nodeIdentifier();
            }
            throw new RuntimeException("should not happen");
        }

        OUTBOUND_URI(String uri) { this.uri = uri; }
    }

    // Method to provide a string representation of the Node object

    @Override
    public String toString() {
        return "Node{" +
               "nodeInfo=" + nodeInfo +
               '}';
    }

}
