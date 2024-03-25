/**
 * This class represents a sensor node in the sensor network system.
 * It communicates with the registry for registration and with other nodes for peer-to-peer communication.
 * The node processes queries and executes them either locally or forwards them to neighboring nodes.
 */
package components.node;

import ast.query.Query;
import components.ConnectorNodeP2P;
import cvm.CVM;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.*;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import fr.sorbonne_u.utils.aclocks.*;
import logger.CustomTraceWindow;
import requests.ExecutionState;
import requests.NodeInfo;
import requests.ProcessingNode;
import requests.RequestContinuation;
import visualization.Visualisation;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static visualization.Visualisation.addProcessingNode;

@OfferedInterfaces(offered={ NodeClientInCI.class, NodeP2PInCI.class })
@RequiredInterfaces(required={ RegistrationCI.class, NodeP2POutCI.class, ClocksServerCI.class })
public class Node
    extends AbstractComponent
    implements SensorNodeP2PImplI {

    private static int nth = 0;
    private final int endDelay;
    protected NodePortFromClient clientInboundPort;
    protected NodePortForRegistry registryOutboundPort;
    protected NodeInfo nodeInfo;
    protected ProcessingNodeI processingNode;
    protected ClocksServerOutboundPort clockPort;

    protected HashMap<Direction, NodePortForP2P> portsForP2P = new HashMap<>();
    protected NodePortFromP2P portFromP2P;
    long startDelay;

    /**
     * Constructs a new sensor node with the given node information and sensor data.
     * Initializes ports for communication and toggles logging and tracing.
     *
     * @param nodeInfo   the information of the sensor node
     * @param sensorData the sensor data collected by the node
     * @throws Exception if an error occurs during initialization
     */
    protected Node(NodeInfo nodeInfo, Set<SensorDataI> sensorData, int endDelay) throws Exception {
        super(1, 1);
        this.nodeInfo = nodeInfo;
        this.endDelay = endDelay;
        this.processingNode = new ProcessingNode(
            nodeInfo.nodeIdentifier(),
            nodeInfo.nodePosition(),
            new HashSet<>(),
            sensorData
        );
        addProcessingNode(this.processingNode.getNodeIdentifier(), this.processingNode);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        CustomTraceWindow tracerWindow = new CustomTraceWindow(
            nodeInfo.nodeIdentifier(),
            0, 0,
            screenSize.width / 3, screenSize.height / 5,
            nth % 3, (nth / 3) % 3
        );
        setTracer(tracerWindow);

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

        this.clockPort = new ClocksServerOutboundPort(uri(OUTBOUND_URI.CLOCK), this);
        this.clockPort.publishPort();

        this.toggleLogging();
        this.toggleTracing();
        this.logMessage(this.nodeInfo.nodeIdentifier());
        this.startDelay = (nth + 1) * 60L;
        nth++;
    }


    /**
     * Executes the sensor node.
     * Registers with the registry and establishes connections with neighboring nodes.
     *
     * @throws Exception if an error occurs during execution
     */
    @Override
    public void execute() throws Exception {
        super.execute();

        this.doPortConnection(
            this.clockPort.getPortURI(),
            ClocksServer.STANDARD_INBOUNDPORT_URI,
            ClocksServerConnector.class.getCanonicalName()
        );
        AcceleratedClock clock = this.clockPort.getClock(CVM.CLOCK_URI);
        clock.waitUntilStart();
        long delay = clock.nanoDelayUntilInstant(clock.currentInstant().plusSeconds(startDelay));

        this.scheduleTask(f -> {
            try {
                Set<NodeInfoI> neighbours = this.registryOutboundPort.register(this.nodeInfo);
                for (NodeInfoI neighbour : neighbours) {
                    try {
                        ask4Connection(neighbour);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            logMessage(nodeInfo.nodeIdentifier() + ": neighbours = " + processingNode.getNeighbours());
            System.out.println(nodeInfo.nodeIdentifier() + ": neighbours = " + processingNode.getNeighbours());
        }, delay, TimeUnit.NANOSECONDS);

        long endDelayNano = clock.nanoDelayUntilInstant(clock.currentInstant().plusSeconds(this.endDelay));

        this.scheduleTask(f -> {
            try {
                while (!this.processingNode.getNeighbours().isEmpty()) {
                    try {
                        // noinspection OptionalGetWithoutIsPresent
                        NodeInfoI neighbour = this.processingNode.getNeighbours().stream().findFirst().get();
                        ask4Disconnection(neighbour);
                    } catch (Exception e) {
                        System.err.println("e = " + e);
                    }
                }
                this.registryOutboundPort.unregister(this.nodeInfo.nodeIdentifier());
            } catch (Exception e) {
                System.err.println("e = " + e);
            }

            logMessage(nodeInfo.nodeIdentifier() + ": disconnected + unregistred ");
        }, endDelayNano, TimeUnit.NANOSECONDS);

    }

    /**
     * Shuts down the sensor node.
     * Unpublishes ports and shuts down gracefully.
     *
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
            this.clockPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    /**
     * Finalizes the sensor node.
     * Disconnects from ports and performs necessary cleanup.
     *
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
        this.doPortDisconnection(uri(OUTBOUND_URI.CLOCK));
        super.finalise();
    }

    /**
     * Executes a request on the sensor node.
     * Processes the request locally or forwards it to neighboring nodes based on the query evaluation.
     *
     * @param request the request to be executed
     * @return the result of the query execution
     * @throws Exception if an error occurs during execution
     */
    public QueryResultI execute(RequestI request) throws Exception {
        assert request.getQueryCode() instanceof Query;
        Query query = (Query) request.getQueryCode();
        ExecutionState executionState = new ExecutionState(processingNode);
        QueryResultI evaled = query.eval(executionState);
        Visualisation.addRequest(request.requestURI(), this.nodeInfo.nodeIdentifier());
        if (executionState.isDirectional() && executionState.noMoreHops()) {
            return evaled;
        }
        if (executionState.isFlooding()) {
            for (NodeInfoI neighbourInfo : processingNode.getNeighbours()) {
                if (executionState.isNodeNotDone(neighbourInfo.nodeIdentifier()) &&
                    executionState.withinMaximalDistance(neighbourInfo.nodePosition())) {

                    Direction dir = nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition());
                    RequestContinuation requestContinuation = new RequestContinuation(request, executionState);
                    QueryResultI contRes = portsForP2P.get(dir).execute(requestContinuation);

                    evaled.gatheredSensorsValues().addAll(contRes.gatheredSensorsValues());
                    evaled.positiveSensorNodes().addAll(contRes.positiveSensorNodes());
                }
            }
        }
        for (Direction dir : executionState.getDirections()) {
            if (isPortConnected(OUTBOUND_URI.P2P(dir, nodeInfo))) {
                RequestContinuation requestContinuation = new RequestContinuation(request,
                                                                                  executionState.withDirection(dir));
                QueryResultI contRes = portsForP2P.get(dir).execute(requestContinuation);
                evaled.gatheredSensorsValues().addAll(contRes.gatheredSensorsValues());
                evaled.positiveSensorNodes().addAll(contRes.positiveSensorNodes());
            }
        }
        return evaled;
    }

    /**
     * Executes a request asynchronously.
     *
     * @param requestContinuation the continuation of the request to execute
     * @throws Exception if an error occurs during execution
     */
    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {

    }

    /**
     * Executes a request asynchronously.
     *
     * @param request the request to execute
     * @throws Exception if an error occurs during execution
     */
    public void executeAsync(RequestI request) throws Exception {
        assert request.getQueryCode() instanceof Query;
        Query query = (Query) request.getQueryCode();
        ExecutionState executionState = new ExecutionState(processingNode);
        QueryResultI evaled = query.eval(executionState);
        Visualisation.addRequest(request.requestURI(), this.nodeInfo.nodeIdentifier());
        if (executionState.isDirectional() && executionState.noMoreHops()) {
            return;
        }
        if (executionState.isFlooding()) {
            for (NodeInfoI neighbourInfo : processingNode.getNeighbours()) {

            }
        }
    }

    /**
     * Executes a continuation of a request.
     * Processes the continuation and returns the query result.
     *
     * @param request the continuation of the request to execute
     * @return the query result
     * @throws Exception if an error occurs during execution
     */
    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        ExecutionStateI execState = request.getExecutionState();
        execState.incrementHops();
        execState.updateProcessingNode(this.processingNode);
        assert execState instanceof ExecutionState;

        QueryResultI evaled = ((Query) request.getQueryCode()).eval(execState);
        Visualisation.addRequest(request.requestURI(), this.nodeInfo.nodeIdentifier());
        if (execState.isDirectional()) {
            for (Direction dir : execState.getDirections()) {
                if (isPortConnected(OUTBOUND_URI.P2P(dir, nodeInfo))) {
                    QueryResultI contEvaled = portsForP2P.get(dir).execute(request);
                    evaled.gatheredSensorsValues().addAll(contEvaled.gatheredSensorsValues());
                    evaled.positiveSensorNodes().addAll(contEvaled.positiveSensorNodes());
                }
            }
        } else if (execState.isFlooding()) {
            for (NodeInfoI neighbourInfo : processingNode.getNeighbours()) {
                if (((ExecutionState) execState).isNodeNotDone(neighbourInfo.nodeIdentifier()) &&
                    execState.withinMaximalDistance(neighbourInfo.nodePosition())) {

                    RequestContinuation requestContinuation = new RequestContinuation(request, execState);
                    Direction dir = nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition());
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
     *
     * @param neighbour the neighboring node to connect to
     * @throws Exception if an error occurs during the connection process
     */
    @Override
    public void ask4Connection(NodeInfoI neighbour) throws Exception {
        Direction dir = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
        logMessage(nodeInfo.nodeIdentifier() +
                   ": ask4Connection(requesting) -> " +
                   neighbour.nodeIdentifier() +
                   " dir: " +
                   dir);

        EndPointDescriptorI endPointInfo = neighbour.p2pEndPointInfo();
        assert endPointInfo instanceof NodeInfo.EndPointInfo;

        this.doPortConnection(
            OUTBOUND_URI.P2P(dir, nodeInfo),
            endPointInfo.toString(),
            ConnectorNodeP2P.class.getCanonicalName()
        );
        portsForP2P.get(dir).ask4Connection(this.nodeInfo);
        this.processingNode.getNeighbours().add(neighbour);
        logMessage(
            nodeInfo.nodeIdentifier() + ": ask4Connection(done) -> " + neighbour.nodeIdentifier() + " dir: " + dir);
    }

    /**
     * Requests disconnection from a neighboring node.
     *
     * @param neighbour the neighboring node to disconnect from
     * @throws Exception if an error occurs during the disconnection process
     */
    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        Direction dir = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
        logMessage(nodeInfo.nodeIdentifier() +
                   ": ask4Disconnection(requesting) -> " +
                   neighbour.nodeIdentifier() +
                   " dir: " +
                   dir);

        EndPointDescriptorI endPointInfo = neighbour.p2pEndPointInfo();
        assert endPointInfo instanceof NodeInfo.EndPointInfo;
        portsForP2P.get(dir).ask4Disconnection(this.nodeInfo);
        this.doPortDisconnection(OUTBOUND_URI.P2P(dir, nodeInfo));

        Set<NodeInfoI> neighbours = this.processingNode.getNeighbours();
        neighbours.remove(neighbour);
        logMessage(
            nodeInfo.nodeIdentifier() + ": ask4Disconnection(done) -> " + neighbour.nodeIdentifier() + " dir: " + dir);
    }

    /**
     * Connects to a neighboring node.
     *
     * @param neighbour the neighboring node to connect to
     * @throws Exception if an error occurs during the connection process
     */
    public void connect(NodeInfoI neighbour) throws Exception {
        Direction dir = nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
        logMessage(nodeInfo.nodeIdentifier() + ": connect(request) <- " + neighbour.nodeIdentifier() + " dir: " + dir);
        Set<NodeInfoI> neighbours = processingNode.getNeighbours();
        PositionI position = nodeInfo.nodePosition();
        Optional<NodeInfoI> currentNeighbour =
            neighbours.stream()
                      .filter(cn -> position.directionFrom(cn.nodePosition()).equals(dir))
                      .findFirst();
        if (currentNeighbour.isPresent()) {
            NodeInfoI nodeInfoVoisin = currentNeighbour.get();
            ask4Disconnection(nodeInfoVoisin);
            neighbours.remove(nodeInfoVoisin);
        }
        this.doPortConnection(
            OUTBOUND_URI.P2P(dir, nodeInfo),
            neighbour.p2pEndPointInfo().toString(),
            ConnectorNodeP2P.class.getCanonicalName());
        neighbours.add(neighbour);
        logMessage(nodeInfo.nodeIdentifier() + ": connect(done) <- " + neighbour.nodeIdentifier() + " dir: " + dir);
    }

    /**
     * Disconnects from a neighboring node.
     *
     * @param neighbour the neighboring node to disconnect from
     * @throws Exception if an error occurs during the disconnection process
     */
    public void disconnect(NodeInfoI neighbour) throws Exception {
        Direction dir = nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
        logMessage(nodeInfo.nodeIdentifier() + ": disconnect(request)<-" + neighbour.nodeIdentifier() + " dir: " + dir);
        processingNode.getNeighbours().remove(neighbour);
        this.doPortDisconnection(OUTBOUND_URI.P2P(dir, nodeInfo));
        NodeInfoI newNeighbour = this.registryOutboundPort.findNewNeighbour(nodeInfo, dir);
        if (newNeighbour != null && !newNeighbour.equals(neighbour) && !newNeighbour.equals(nodeInfo)) {
            ask4Connection(newNeighbour);
        }
        logMessage(nodeInfo.nodeIdentifier() + ": disconnect(done)<-" + neighbour.nodeIdentifier() + " dir: " + dir);
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
        REGISTRY("node-outbound-registry-"),
        CLOCK("node-clock-uri-");

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
