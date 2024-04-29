package components.node;

import ast.query.Query;
import components.ConnectorNodeClient;
import components.ConnectorNodeP2P;
import components.ConnectorNodeRegistry;
import components.node.inbound_ports.NodeP2PInPort;
import components.node.inbound_ports.NodeRequestingInPort;
import components.node.outbound_ports.NodeP2POutPort;
import components.node.outbound_ports.NodeRegistrationOutPort;
import components.node.outbound_ports.NodeReqResultOutPort;
import components.registry.Registry;
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
import parsers.NodeParser;
import sensor_network.*;
import sensor_network.requests.ExecutionState;
import sensor_network.requests.ProcessingNode;
import sensor_network.requests.RequestContinuation;
import visualization.Visualisation;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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

    protected final long endDelay;
    protected final long startDelay;
    protected final long sensorUpdateDelay;
    protected final NodeInfo nodeInfo;
    protected final ProcessingNodeI processingNode;
    protected final HashMap<Direction, NodeP2POutPort> portsForP2P;
    protected final Map<String, SensorDataI> sensorDataMap;
    protected final Map<String, Float> sensorDataUpdateMap;
    protected final List<String> processedRequests;

    protected final NodeRequestingInPort requestingInPort;
    protected final NodeRegistrationOutPort registrationOutPort;
    protected final ClocksServerOutboundPort clockOutPort;
    protected final NodeP2PInPort p2PInPort;

    /**
     * Constructs a new Node component object.
     *
     * @param nodeData         the data of the node parsed from the config file
     * @param inboundPortUris  the URIs of inbound ports
     * @param outboundPortUris the URIs of outbound ports
     * @throws Exception if an error occurs during construction
     */
    protected Node(
        NodeParser.Node nodeData,
        Map<PortName, String> inboundPortUris,
        Map<PortName, String> outboundPortUris
    ) throws Exception {
        super(8, 8);
        this.nodeInfo = new NodeInfo(nodeData.range, nodeData.id, new Position(nodeData.position.x, nodeData.position.y));
        this.endDelay = nodeData.endAfter;
        this.startDelay = nodeData.startAfter;
        this.sensorUpdateDelay = nodeData.sensorUpdateDelay;
        this.sensorDataMap = nodeData.sensors
            .stream()
            .map(sd -> new SensorData<>(nodeInfo.nodeIdentifier(), sd.id, sd.value, Instant.now()))
            .collect(Collectors.toMap(SensorData::getSensorIdentifier, data -> data));
        this.sensorDataUpdateMap = nodeData.sensors
            .stream()
            .collect(Collectors.toMap(sensor -> sensor.id, sensor -> sensor.toAdd));
        this.processingNode = new ProcessingNode(nodeInfo.nodeIdentifier(), nodeInfo.nodePosition(), new HashSet<>(), sensorDataMap);
        this.processedRequests = new CopyOnWriteArrayList<>();
        Visualisation.addProcessingNode(this.processingNode.getNodeIdentifier(), this.processingNode);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        CustomTraceWindow tracerWindow = new CustomTraceWindow(
            nodeInfo.nodeIdentifier(), 0, 0,
            screenSize.width / 3, screenSize.height / 5,
            nth % 3, (nth / 3) % 3
        );
        setTracer(tracerWindow);

        this.requestingInPort = new NodeRequestingInPort(inboundPortUris.get(PortName.REQUESTING), this);
        this.requestingInPort.publishPort();

        this.registrationOutPort = new NodeRegistrationOutPort(outboundPortUris.get(PortName.REGISTRATION), this);
        this.registrationOutPort.publishPort();

        this.portsForP2P = new HashMap<>();
        for (Direction dir : Direction.values()) {
            NodeP2POutPort port = new NodeP2POutPort(outboundPortUris.get(PortName.P2P) + "-" + dir.name(), this);
            port.publishPort();
            portsForP2P.put(dir, port);
        }

        this.p2PInPort = new NodeP2PInPort(inboundPortUris.get(PortName.P2P), this);
        this.p2PInPort.publishPort();

        this.nodeInfo.setEndPointInfo(new BCM4JavaEndPointDescriptor(inboundPortUris.get(PortName.REQUESTING), RequestingCI.class));
        this.nodeInfo.setP2pEndPointInfo(new BCM4JavaEndPointDescriptor(inboundPortUris.get(PortName.P2P), SensorNodeP2PCI.class));

        this.clockOutPort = new ClocksServerOutboundPort(outboundPortUris.get(PortName.CLOCK), this);
        this.clockOutPort.publishPort();

        this.toggleLogging();
        this.toggleTracing();
        this.logMessage(this.nodeInfo.nodeIdentifier());
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

        this.clockOutPort.doConnection(ClocksServer.STANDARD_INBOUNDPORT_URI, new ClocksServerConnector());
        AcceleratedClock clock = this.clockOutPort.getClock(CVM.CLOCK_URI);
        clock.waitUntilStart();
        Instant baseInstant = clock.currentInstant();

        long startDelayNano = clock.nanoDelayUntilInstant(baseInstant.plusSeconds(this.startDelay));
        long updateDelayNano = clock.nanoDelayUntilInstant(baseInstant.plusSeconds(this.sensorUpdateDelay));
        long endDelayNano = clock.nanoDelayUntilInstant(baseInstant.plusSeconds(this.endDelay));

        // sensorData
        this.scheduleTaskAtFixedRate(f -> {
            for (Map.Entry<String, Float> entry : this.sensorDataUpdateMap.entrySet()) {
                String sensorId = entry.getKey();
                Float toAdd = entry.getValue();
                this.sensorDataMap.computeIfPresent(
                    sensorId,
                    (id, current) -> new SensorData<>(
                        current.getNodeIdentifier(),
                        id,
                        current.getValue() instanceof Number
                            ? ((Number) current.getValue()).doubleValue() + toAdd
                            : !(Boolean) current.getValue(),
                        current.getTimestamp().plusSeconds(this.sensorUpdateDelay)
                    )
                );
            }
        }, startDelayNano + updateDelayNano, updateDelayNano, TimeUnit.NANOSECONDS);

        // ask4connection
        this.scheduleTask(f -> {
            try {
                this.registrationOutPort.doConnection(Registry.INBOUND_URI.REGISTRATION.uri, new ConnectorNodeRegistry());
                Set<NodeInfoI> neighbours = this.registrationOutPort.register(this.nodeInfo);
                for (NodeInfoI neighbour : neighbours) {
                    Direction dir = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
                    logMessage(nodeInfo.nodeIdentifier() + ": ask4Connection(requesting) -> " + neighbour.nodeIdentifier() + " dir: " + dir);
                    this.portsForP2P.get(dir).doConnection(neighbour.p2pEndPointInfo().toString(), ConnectorNodeP2P.class.getCanonicalName());
                    this.portsForP2P.get(dir).ask4Connection(this.nodeInfo);
                    this.processingNode.getNeighbours().add(neighbour);
                    logMessage(nodeInfo.nodeIdentifier() + ": ask4Connection(done) -> " + neighbour.nodeIdentifier() + " dir: " + dir);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
            logMessage(nodeInfo.nodeIdentifier() + ": neighbours = " + processingNode.getNeighbours());
            System.out.println(nodeInfo.nodeIdentifier() + ": neighbours = " + processingNode.getNeighbours());
        }, startDelayNano, TimeUnit.NANOSECONDS);


        // ask4disconnection
        this.scheduleTask(f -> {
            try {
                while (!this.processingNode.getNeighbours().isEmpty()) {
                    NodeInfoI neighbour = this.processingNode.getNeighbours().iterator().next();
                    Direction dir = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
                    logMessage(nodeInfo.nodeIdentifier() + ": ask4Disconnection(requesting) -> " + neighbour.nodeIdentifier() + " dir: " + dir);
                    this.portsForP2P.get(dir).ask4Disconnection(this.nodeInfo);
                    logMessage(nodeInfo.nodeIdentifier() + ": ask4Disconnection(done) -> " + neighbour.nodeIdentifier() + " dir: " + dir);
                    this.processingNode.getNeighbours().remove(neighbour);
                }
                this.registrationOutPort.unregister(this.nodeInfo.nodeIdentifier());
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
        ExecutionState execState = new ExecutionState(processingNode);

        if (requestAlreadyProcessed(request.requestURI())) {
            sendBackToClient(request, execState.getCurrentResult());
            return;
        }
        this.addToProcessedRequests(request.requestURI());

        Query query = (Query) request.getQueryCode();
        execState.addToCurrentResult(query.eval(execState));
        Visualisation.addRequest(request.requestURI(), this.nodeInfo.nodeIdentifier());

        if (execState.isFlooding()) {
            for (NodeInfoI neighbourInfo : processingNode.getNeighbours()) {
                Direction dir = nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition());
                portsForP2P.get(dir).executeAsync(new RequestContinuation(request, execState));
            }
        } else {
            List<Direction> propagationDirections = getPropagationDirections(execState);
            for (Direction dir : propagationDirections) {
                portsForP2P.get(dir).executeAsync(new RequestContinuation(request, execState));
            }

            if (propagationDirections.isEmpty()) {
                sendBackToClient(request, execState.getCurrentResult());
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

        if (requestAlreadyProcessed(request.requestURI())) {
            sendBackToClient(request, execState.getCurrentResult());
            return;
        }
        this.addToProcessedRequests(request.requestURI());

        if ((execState.isDirectional() && execState.noMoreHops())
            || (execState.isFlooding() && !execState.withinMaximalDistance(this.nodeInfo.nodePosition()))) {
            sendBackToClient(request, execState.getCurrentResult());
            return;
        }

        execState.updateProcessingNode(this.processingNode);
        execState.addToCurrentResult(((Query) request.getQueryCode()).eval(execState));
        Visualisation.addRequest(request.requestURI(), this.nodeInfo.nodeIdentifier());

        if (execState.isFlooding()) {
            for (NodeInfoI neighbourInfo : processingNode.getNeighbours()) {
                portsForP2P.get(nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition()))
                           .executeAsync(new RequestContinuation(request, execState));
            }
        } else {
            execState.incrementHops();
            List<Direction> propagationDirections = getPropagationDirections(execState);
            for (Direction dir : propagationDirections) {
                this.portsForP2P.get(dir).executeAsync(new RequestContinuation(request, execState));
            }

            if (propagationDirections.isEmpty()) {
                sendBackToClient(request, execState.getCurrentResult());
            }
        }
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
        ExecutionState execState = new ExecutionState(this.processingNode);
        execState.addToCurrentResult(query.eval(execState));
        Visualisation.addRequest(request.requestURI(), this.nodeInfo.nodeIdentifier());
        this.addToProcessedRequests(request.requestURI());

        if (execState.isDirectional() && execState.noMoreHops()) {
            return execState.getCurrentResult();
        }

        if (execState.isFlooding()) {
            for (NodeInfoI neighbourInfo : this.processingNode.getNeighbours()) {
                if (execState.withinMaximalDistance(neighbourInfo.nodePosition())) {
                    portsForP2P.get(nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition()))
                               .execute(new RequestContinuation(request, execState));
                }
            }
        } else {
            for (Direction dir : execState.getDirections()) {
                if (this.portsForP2P.get(dir).connected()) {
                    portsForP2P.get(dir).execute(new RequestContinuation(request, execState));
                }
            }
        }
        return execState.getCurrentResult();
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
        assert request.getExecutionState() instanceof ExecutionState;
        if (requestAlreadyProcessed(request.requestURI())) return null;
        this.addToProcessedRequests(request.requestURI());

        ExecutionStateI execState = request.getExecutionState();
        if (execState.isDirectional() && execState.noMoreHops()) return null;

        execState.updateProcessingNode(this.processingNode);
        execState.addToCurrentResult(((Query) request.getQueryCode()).eval(execState));
        Visualisation.addRequest(request.requestURI(), this.nodeInfo.nodeIdentifier());

        if (execState.isFlooding()) {
            for (NodeInfoI neighbourInfo : processingNode.getNeighbours()) {
                if (execState.withinMaximalDistance(neighbourInfo.nodePosition())) {
                    portsForP2P.get(nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition()))
                               .execute(new RequestContinuation(request, execState));
                }
            }
        } else {
            execState.incrementHops();
            for (Direction dir : execState.getDirections()) {
                if (this.portsForP2P.get(dir).connected()) {
                    portsForP2P.get(dir).execute(request);
                }
            }
        }
        return execState.getCurrentResult();
    }

    /**
     * Requests a connection to a neighboring node.
     *
     * @param neighbour the neighboring node to connect to
     * @throws Exception if an error occurs during the connection process
     */
    @Override
    public void ask4Connection(NodeInfoI neighbour) throws Exception {
    }

    /**
     * Requests disconnection from a neighboring node.
     *
     * @param neighbour the neighboring node to disconnect from
     * @throws Exception if an error occurs during the disconnection process
     */
    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
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
            this.requestingInPort.unpublishPort();
            this.registrationOutPort.unpublishPort();
            this.p2PInPort.unpublishPort();
            for (NodeP2POutPort port : this.portsForP2P.values()) {
                port.unpublishPort();
            }
            this.clockOutPort.unpublishPort();
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
        for (NodeP2POutPort port : this.portsForP2P.values()) {
            if (port.connected()) port.doDisconnection();
        }
        this.registrationOutPort.doDisconnection();
        this.clockOutPort.doDisconnection();
        super.finalise();
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
        String portUri = this.nodeInfo.nodeIdentifier() + ":out:" + connInfo.endPointInfo().toString();
        NodeReqResultOutPort port = new NodeReqResultOutPort(portUri, this);

        port.publishPort();
        port.doConnection(connInfo.endPointInfo().toString(), new ConnectorNodeClient());
        port.acceptRequestResult(request.requestURI(), result);
        port.doDisconnection();
        port.unpublishPort();
        port.destroyPort();
    }


    /**
     * Retrieves the list of directions for query propagation based on the given execution state.
     *
     * @param execState the execution state containing the continuation directions
     * @return a list of directions indicating where the query should be propagated
     * @throws Exception if an error occurs during the retrieval of directions
     */
    private List<Direction> getPropagationDirections(ExecutionStateI execState) throws Exception {
        List<Direction> connectedDirections = new ArrayList<>();
        for (Direction dir : execState.getDirections()) {
            if (this.portsForP2P.get(dir).connected()) {
                connectedDirections.add(dir);
            }
        }
        return connectedDirections;
    }

    protected synchronized boolean requestAlreadyProcessed(String requestUri) {
        return processedRequests.contains(requestUri);
    }

    protected synchronized void removeProcessedRequest(String requestUri) {
        processedRequests.remove(requestUri);
    }

    protected synchronized void addToProcessedRequests(String requestUri) {
        this.processedRequests.add(requestUri);
        this.scheduleTask(f -> removeProcessedRequest(requestUri), 200, TimeUnit.MILLISECONDS);
    }

    // Method to provide a string representation of the Node object

    @Override
    public String toString() {
        return "Node{" + "nodeInfo=" + nodeInfo + '}';
    }

}
