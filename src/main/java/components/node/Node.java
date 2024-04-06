package components.node;

import ast.query.Query;
import components.ConnectorNodeClient;
import components.ConnectorNodeP2P;
import cvm.CVM;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.*;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingImplI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import fr.sorbonne_u.utils.aclocks.*;
import logger.CustomTraceWindow;
import sensor_network.BCM4JavaEndPointDescriptor;
import sensor_network.EndPointInfo;
import sensor_network.NodeInfo;
import sensor_network.requests.ExecutionState;
import sensor_network.requests.ProcessingNode;
import sensor_network.requests.RequestContinuation;
import visualization.Visualisation;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static visualization.Visualisation.addProcessingNode;

/**
 * This class represents a sensor node in the sensor network system.
 * It communicates with the registry for registration and with other nodes for peer-to-peer communication.
 * The node processes queries and executes them either locally or forwards them to neighboring nodes.
 */
@OfferedInterfaces(offered={ RequestingCI.class, SensorNodeP2PCI.class })
@RequiredInterfaces(required={ RegistrationCI.class, SensorNodeP2PCI.class, ClocksServerCI.class, RequestResultCI.class })
public class Node
    extends AbstractComponent
    implements SensorNodeP2PImplI, RequestingImplI {

    protected static int nth = 0;
    protected final int endDelay;
    protected final HashMap<SensorDataI, Float> sensorDataAddValueMap;
    protected final NodePortFromClient portFromClient;
    protected final NodePortForRegistry portForRegistry;
    protected final ClocksServerOutboundPort portForClock;
    protected final NodePortFromP2P portFromP2P;
    protected final NodeInfo nodeInfo;
    protected final ProcessingNodeI processingNode;
    protected final HashMap<Direction, NodePortForP2P> portsForP2P = new HashMap<>();
    protected final long startDelay;

    /**
     * Constructs a new sensor node with the given node information and sensor data.
     * Initializes ports for communication and toggles logging and tracing.
     *
     * @param nodeInfo   the information of the sensor node
     * @param sensorData the sensor data collected by the node
     * @throws Exception if an error occurs during initialization
     */
    protected Node(NodeInfo nodeInfo, HashMap<SensorDataI, Float> sensorData, int endDelay) throws Exception {
        super(1, 1);
        this.nodeInfo = nodeInfo;
        this.endDelay = endDelay;
        this.processingNode = new ProcessingNode(nodeInfo.nodeIdentifier(), nodeInfo.nodePosition(), new HashSet<>(), sensorData.keySet());
        this.sensorDataAddValueMap = sensorData;
        addProcessingNode(this.processingNode.getNodeIdentifier(), this.processingNode);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        CustomTraceWindow tracerWindow = new CustomTraceWindow(
            nodeInfo.nodeIdentifier(), 0, 0,
            screenSize.width / 3, screenSize.height / 5,
            nth % 3, (nth / 3) % 3
        );
        setTracer(tracerWindow);

        this.portFromClient = new NodePortFromClient(INBOUND_URI.CLIENT.of(nodeInfo), this);
        this.portFromClient.publishPort();

        this.portForRegistry = new NodePortForRegistry(OUTBOUND_URI.REGISTRY.of(nodeInfo), this);
        this.portForRegistry.publishPort();

        for (Direction dir : Direction.values()) {
            NodePortForP2P port = new NodePortForP2P(OUTBOUND_URI.P2P(dir, nodeInfo), this);
            port.publishPort();
            portsForP2P.put(dir, port);
        }

        this.portFromP2P = new NodePortFromP2P(INBOUND_URI.P2P.of(nodeInfo), this);
        this.portFromP2P.publishPort();

        this.nodeInfo.setEndPointInfo(new EndPointInfo(INBOUND_URI.CLIENT.of(nodeInfo)));
        this.nodeInfo.setP2pEndPointInfo(new BCM4JavaEndPointDescriptor(INBOUND_URI.P2P.of(nodeInfo), SensorNodeP2PCI.class));

        this.portForClock = new ClocksServerOutboundPort(OUTBOUND_URI.CLOCK.of(nodeInfo), this);
        this.portForClock.publishPort();

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
        Thread.currentThread().setName(nodeInfo.nodeIdentifier());

        this.doPortConnection(this.portForClock.getPortURI(), ClocksServer.STANDARD_INBOUNDPORT_URI, ClocksServerConnector.class.getCanonicalName());
        AcceleratedClock clock = this.portForClock.getClock(CVM.CLOCK_URI);
        clock.waitUntilStart();

        // ask4connection
        long delay = clock.nanoDelayUntilInstant(clock.currentInstant().plusSeconds(startDelay));
        this.scheduleTask(f -> {
            try {
                Set<NodeInfoI> neighbours = this.portForRegistry.register(this.nodeInfo);
                for (NodeInfoI neighbour : neighbours) {
                    Direction dir = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
                    this.portsForP2P.get(dir).doConnection(neighbour.p2pEndPointInfo().toString(), ConnectorNodeP2P.class.getCanonicalName());
                    this.portsForP2P.get(dir).ask4Connection(this.nodeInfo);
                    this.processingNode.getNeighbours().add(neighbour);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
            logMessage(nodeInfo.nodeIdentifier() + ": neighbours = " + processingNode.getNeighbours());
            System.out.println(nodeInfo.nodeIdentifier() + ": neighbours = " + processingNode.getNeighbours());
        }, delay, TimeUnit.NANOSECONDS);


        // ask4disconnection
        long endDelayNano = clock.nanoDelayUntilInstant(clock.currentInstant().plusSeconds(this.endDelay));
        this.scheduleTask(f -> {
            try {
                while (!this.processingNode.getNeighbours().isEmpty()) {
                    NodeInfoI neighbour = this.processingNode.getNeighbours().iterator().next();
                    Direction dir = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
                    this.portsForP2P.get(dir).ask4Disconnection(this.nodeInfo);
                    this.portsForP2P.get(dir).doDisconnection();
                    this.processingNode.getNeighbours().remove(neighbour);
                }
                this.portForRegistry.unregister(this.nodeInfo.nodeIdentifier());
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }

            logMessage(nodeInfo.nodeIdentifier() + ": disconnected + unregistered ");
        }, endDelayNano, TimeUnit.NANOSECONDS);

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
        QueryResultI result = query.eval(executionState);
        Visualisation.addRequest(request.requestURI(), this.nodeInfo.nodeIdentifier());

        sendBackToClient(request, result);

        if (executionState.isDirectional() && executionState.noMoreHops()) {
            return;
        }
        if (executionState.isFlooding()) {
            for (NodeInfoI neighbourInfo : processingNode.getNeighbours()) {
                if (executionState.isNodeNotDone(neighbourInfo.nodeIdentifier()) && executionState.withinMaximalDistance(neighbourInfo.nodePosition())) {

                    Direction dir = nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition());
                    portsForP2P.get(dir).executeAsync(new RequestContinuation(request, executionState));
                }
            }
        } else {
            for (Direction dir : executionState.getDirections()) {
                if (isPortConnected(OUTBOUND_URI.P2P(dir, nodeInfo))) {
                    portsForP2P.get(dir).executeAsync(new RequestContinuation(request, executionState.withDirection(dir)));
                }
            }
        }
    }

    /**
     * Executes a request asynchronously.
     *
     * @param request the continuation of the request to execute
     * @throws Exception if an error occurs during execution
     */
    @Override
    public void executeAsync(RequestContinuationI request) throws Exception {
        assert request.getQueryCode() instanceof Query;
        ExecutionStateI execState = request.getExecutionState();
        execState.updateProcessingNode(this.processingNode);

        if (execState.isDirectional() && execState.noMoreHops()) return;

        QueryResultI eval = ((Query) request.getQueryCode()).eval(execState);
        Visualisation.addRequest(request.requestURI(), this.nodeInfo.nodeIdentifier());
        sendBackToClient(request, eval);

        if (execState.isFlooding()) {
            execState.incrementHops();
            assert execState instanceof ExecutionState;
            for (NodeInfoI neighbourInfo : processingNode.getNeighbours()) {
                if (((ExecutionState) execState).isNodeNotDone(neighbourInfo.nodeIdentifier()) &&
                    execState.withinMaximalDistance(neighbourInfo.nodePosition())) {

                    Direction dir = nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition());
                    portsForP2P.get(dir).executeAsync(new RequestContinuation(request, execState));
                }
            }
        } else {
            for (Direction dir : execState.getDirections()) {
                if (isPortConnected(OUTBOUND_URI.P2P(dir, nodeInfo))) {
                    portsForP2P.get(dir).executeAsync(new RequestContinuation(request, execState));
                }
            }
        }
    }

    /**
     * Sends the query result back to the client.
     *
     * @param request the original request
     * @param result  the query result
     * @throws Exception if an error occurs during the process
     */
    private void sendBackToClient(RequestI request, QueryResultI result) throws Exception {
        ConnectionInfoI connInfo = request.clientConnectionInfo();

        NodePortForClient port = new NodePortForClient(OUTBOUND_URI.CLIENT.of(this.nodeInfo, connInfo.nodeIdentifier()), this);
        port.publishPort();
        port.doConnection(connInfo.endPointInfo().toString(), ConnectorNodeClient.class.getCanonicalName());
        port.acceptRequestResult(request.requestURI(), result);
        port.doDisconnection();
        port.unpublishPort();
        port.destroyPort();
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
        ExecutionState execState = new ExecutionState(processingNode);
        QueryResultI result = query.eval(execState);
        Visualisation.addRequest(request.requestURI(), this.nodeInfo.nodeIdentifier());
        if (execState.isDirectional() && execState.noMoreHops()) {
            return result;
        }
        if (execState.isFlooding()) {
            for (NodeInfoI neighbourInfo : processingNode.getNeighbours()) {
                if (execState.withinMaximalDistance(neighbourInfo.nodePosition())) {
                    Direction dir = nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition());
                    RequestContinuation requestContinuation = new RequestContinuation(request, execState);
                    QueryResultI contRes = portsForP2P.get(dir).execute(requestContinuation);

                    result.gatheredSensorsValues().addAll(contRes.gatheredSensorsValues());
                    result.positiveSensorNodes().addAll(contRes.positiveSensorNodes());
                }
            }
        } else {
            for (Direction dir : execState.getDirections()) {
                if (isPortConnected(OUTBOUND_URI.P2P(dir, nodeInfo))) {
                    QueryResultI contRes = portsForP2P.get(dir).execute(new RequestContinuation(request, execState));
                    result.gatheredSensorsValues().addAll(contRes.gatheredSensorsValues());
                    result.positiveSensorNodes().addAll(contRes.positiveSensorNodes());
                }
            }
        }
        return result;
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
        execState.updateProcessingNode(this.processingNode);

        QueryResultI evaled = ((Query) request.getQueryCode()).eval(execState);
        Visualisation.addRequest(request.requestURI(), this.nodeInfo.nodeIdentifier());

        if (execState.isFlooding()) {
            assert execState instanceof ExecutionState;
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
        } else {
            execState.incrementHops();
            for (Direction dir : execState.getDirections()) {
                if (isPortConnected(OUTBOUND_URI.P2P(dir, nodeInfo))) {
                    QueryResultI contEvaled = portsForP2P.get(dir).execute(request);
                    if (contEvaled.isBooleanRequest()) {
                        evaled.positiveSensorNodes().addAll(contEvaled.positiveSensorNodes());
                    } else {
                        evaled.gatheredSensorsValues().addAll(contEvaled.gatheredSensorsValues());
                    }
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
        Direction dir = nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
        logMessage(nodeInfo.nodeIdentifier() + ": ask4Connection(requesting) -> " + neighbour.nodeIdentifier() + " dir: " + dir);
        Set<NodeInfoI> neighbours = processingNode.getNeighbours();
        PositionI position = nodeInfo.nodePosition();
        Optional<NodeInfoI> currentNeighbour = neighbours.stream().filter(cn -> position.directionFrom(cn.nodePosition()).equals(dir)).findFirst();
        if (currentNeighbour.isPresent()) {
            NodeInfoI nodeInfoVoisin = currentNeighbour.get();
            this.portsForP2P.get(dir).ask4Disconnection(this.nodeInfo);
            this.portsForP2P.get(dir).doDisconnection();
            neighbours.remove(nodeInfoVoisin);
        }
        this.portsForP2P.get(dir).doConnection(neighbour.p2pEndPointInfo().toString(), ConnectorNodeP2P.class.getCanonicalName());
        neighbours.add(neighbour);
        logMessage(nodeInfo.nodeIdentifier() + ": ask4Connection(done) -> " + neighbour.nodeIdentifier() + " dir: " + dir);
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

        logMessage(nodeInfo.nodeIdentifier() + ": ask4Disconnection(requesting) -> " + neighbour.nodeIdentifier() + " dir: " + dir);
        this.portsForP2P.get(dir).doDisconnection();
        processingNode.getNeighbours().remove(neighbour);
        NodeInfoI newNeighbour = this.portForRegistry.findNewNeighbour(nodeInfo, dir);
        if (newNeighbour != null && !newNeighbour.equals(neighbour) && !newNeighbour.equals(nodeInfo)) {
            this.portsForP2P.get(dir).doConnection(newNeighbour.p2pEndPointInfo().toString(), ConnectorNodeP2P.class.getCanonicalName());
            this.portsForP2P.get(dir).ask4Connection(this.nodeInfo);
            this.processingNode.getNeighbours().add(newNeighbour);
        }
        logMessage(nodeInfo.nodeIdentifier() + ": ask4Disconnection(done) -> " + neighbour.nodeIdentifier() + " dir: " + dir);

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
            this.portFromClient.unpublishPort();
            this.portForRegistry.unpublishPort();
            this.portFromP2P.unpublishPort();
            for (NodePortForP2P port : this.portsForP2P.values()) {
                port.unpublishPort();
            }
            this.portForClock.unpublishPort();
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
        for (NodePortForP2P port : this.portsForP2P.values()) {
            if (port.connected()) port.doDisconnection();
        }
        this.portForRegistry.doDisconnection();
        this.portForClock.doDisconnection();
        super.finalise();
    }


    // Enumeration of inbound port URIs
    public enum INBOUND_URI {
        CLIENT("node-inbound-client"), P2P("node-inbound-p2p");

        private final String uri;

        public String of(NodeInfoI nodeInfo) {
            return this.uri + "-" + nodeInfo.nodeIdentifier();
        }

        INBOUND_URI(String uri) { this.uri = uri; }
    }

    // Enumeration of outbound port URIs

    public enum OUTBOUND_URI {
        CLIENT("node-outbound-client"), REGISTRY("node-outbound-registry"), CLOCK("node-clock-uri");

        private final String uri;

        public String of(NodeInfoI nodeInfo) {
            return this.uri + "-" + nodeInfo.nodeIdentifier();
        }

        public String of(NodeInfoI nodeInfo, String otherId) {
            return this.uri + "-" + nodeInfo.nodeIdentifier() + "-" + otherId;
        }

        public static String P2P(Direction dir, NodeInfoI nodeInfo) {
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
        return "Node{" + "nodeInfo=" + nodeInfo + '}';
    }

}
