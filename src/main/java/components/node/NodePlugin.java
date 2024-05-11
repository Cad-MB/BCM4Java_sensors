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
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.cps.sensor_network.interfaces.*;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import fr.sorbonne_u.utils.aclocks.*;
import parsers.NodeParser;
import sensor_network.*;
import sensor_network.requests.ExecutionState;
import sensor_network.requests.ProcessingNode;
import sensor_network.requests.RequestContinuation;
import visualization.Visualisation;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class NodePlugin
    extends AbstractPlugin
    implements SensorNodeP2PImplI, RequestingCI {

    protected final NodeInfo nodeInfo;
    protected final Long endDelay;
    protected final Long startDelay;
    protected final Long sensorUpdateDelay;
    protected final Map<String, SensorDataI> sensorDataMap;
    protected final Map<String, Float> sensorDataUpdateMap;
    protected final Map<PortName, String> inboundPortUris;
    protected final Map<PortName, String> outboundPortUris;
    protected final ProcessingNodeI processingNode;
    protected final List<String> processedRequests;

    protected NodeP2PInPort p2PInPort;
    protected NodeRequestingInPort requestingInPort;

    protected NodeRegistrationOutPort registrationOutPort;
    protected HashMap<Direction, NodeP2POutPort> p2pOutPorts;
    protected ClocksServerOutboundPort clockOutPort;

    public NodePlugin(
        NodeParser.Node nodeData,
        Map<PortName, String> inboundPortUris,
        Map<PortName, String> outboundPortUris
    ) {
        super();
        this.nodeInfo = new NodeInfo(nodeData.range, nodeData.id, new Position(nodeData.position.x, nodeData.position.y));
        this.nodeInfo.setEndPointInfo(new BCM4JavaEndPointDescriptor(inboundPortUris.get(PortName.REQUESTING), RequestingCI.class));
        this.nodeInfo.setP2pEndPointInfo(new BCM4JavaEndPointDescriptor(inboundPortUris.get(PortName.P2P), SensorNodeP2PCI.class));

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
        this.inboundPortUris = inboundPortUris;
        this.outboundPortUris = outboundPortUris;
        this.processingNode = new ProcessingNode(nodeInfo.nodeIdentifier(), nodeInfo.nodePosition(), new HashSet<>(), sensorDataMap);
        this.processedRequests = new CopyOnWriteArrayList<>();
    }

    @Override
    public void installOn(ComponentI owner) throws Exception {
        super.installOn(owner);

        this.addOfferedInterface(RequestingCI.class);
        this.addOfferedInterface(SensorNodeP2PCI.class);

        this.addRequiredInterface(RegistrationCI.class);
        this.addRequiredInterface(SensorNodeP2PCI.class);
        this.addRequiredInterface(RequestResultCI.class);
        this.addRequiredInterface(ClocksServerCI.class);
    }

    @Override
    public void initialise() throws Exception {
        super.initialise();
        Visualisation.addProcessingNode(processingNode.getNodeIdentifier(), processingNode);

        this.p2PInPort = new NodeP2PInPort(inboundPortUris.get(PortName.P2P), this.getOwner(), this.getPluginURI());
        this.p2PInPort.publishPort();

        this.requestingInPort = new NodeRequestingInPort(inboundPortUris.get(PortName.REQUESTING), this.getOwner(), this.getPluginURI());
        this.requestingInPort.publishPort();

        this.registrationOutPort = new NodeRegistrationOutPort(outboundPortUris.get(PortName.REGISTRATION), this.getOwner());
        this.registrationOutPort.publishPort();

        this.p2pOutPorts = new HashMap<>();
        for (Direction dir : Direction.values()) {
            NodeP2POutPort port = new NodeP2POutPort(outboundPortUris.get(PortName.P2P) + "-" + dir.name(), this.getOwner());
            port.publishPort();
            p2pOutPorts.put(dir, port);
        }

        this.clockOutPort = new ClocksServerOutboundPort(outboundPortUris.get(PortName.CLOCK), this.getOwner());
        this.clockOutPort.publishPort();
    }

    public void run() throws Exception {
        Thread.currentThread().setName(nodeInfo.nodeIdentifier());

        this.clockOutPort.doConnection(ClocksServer.STANDARD_INBOUNDPORT_URI, ClocksServerConnector.class.getCanonicalName());
        AcceleratedClock clock = this.clockOutPort.getClock(CVM.CLOCK_URI);
        clock.waitUntilStart();
        Instant baseInstant = clock.currentInstant();

        long startDelayNano = clock.nanoDelayUntilInstant(baseInstant.plusSeconds(this.startDelay));
        long updateDelayNano = clock.nanoDelayUntilInstant(baseInstant.plusSeconds(this.sensorUpdateDelay));
        long endDelayNano = clock.nanoDelayUntilInstant(baseInstant.plusSeconds(this.endDelay));

        // sensorData
        this.getOwner().scheduleTask(f -> {
            for (Map.Entry<String, Float> entry : sensorDataUpdateMap.entrySet()) {
                String sensorId = entry.getKey();
                Float toAdd = entry.getValue();
                sensorDataMap.computeIfPresent(
                    sensorId, (id, current) -> new SensorData<>(
                        current.getNodeIdentifier(),
                        id,
                        current.getValue() instanceof Number
                            ? ((Number) current.getValue()).doubleValue() + toAdd
                            : !(Boolean) current.getValue(),
                        current.getTimestamp().plusSeconds(sensorUpdateDelay)
                    )
                );
            }
        }, startDelayNano + updateDelayNano, TimeUnit.NANOSECONDS);

        // ask4connection
        this.getOwner().scheduleTask(f -> {
            try {
                registrationOutPort.doConnection(Registry.INBOUND_URI.REGISTRATION.uri, ConnectorNodeRegistry.class.getCanonicalName());
                Set<NodeInfoI> neighbours = registrationOutPort.register(nodeInfo);
                for (NodeInfoI neighbour : neighbours) {
                    Direction dir = nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
                    logMessage(nodeInfo.nodeIdentifier() + ": ask4Connection(requesting) -> " + neighbour.nodeIdentifier() + " dir: " + dir);
                    p2pOutPorts.get(dir)
                               .doConnection(((BCM4JavaEndPointDescriptorI) neighbour.p2pEndPointInfo()).getInboundPortURI(),
                                             ConnectorNodeP2P.class.getCanonicalName());
                    p2pOutPorts.get(dir).ask4Connection(nodeInfo);
                    processingNode.getNeighbours().add(neighbour);
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
        this.getOwner().scheduleTask(f -> {
            try {
                while (!processingNode.getNeighbours().isEmpty()) {
                    NodeInfoI neighbour = processingNode.getNeighbours().iterator().next();
                    Direction dir = nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
                    logMessage(nodeInfo.nodeIdentifier() + ": ask4Disconnection(requesting) -> " + neighbour.nodeIdentifier() + " dir: " + dir);
                    p2pOutPorts.get(dir).ask4Disconnection(nodeInfo);
                    logMessage(nodeInfo.nodeIdentifier() + ": ask4Disconnection(done) -> " + neighbour.nodeIdentifier() + " dir: " + dir);
                    processingNode.getNeighbours().remove(neighbour);
                }
                registrationOutPort.unregister(nodeInfo.nodeIdentifier());
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }

            logMessage(nodeInfo.nodeIdentifier() + ": disconnected + unregistered ");
        }, startDelayNano + endDelayNano, TimeUnit.NANOSECONDS);
    }


    @Override
    public void finalise() throws Exception {
        this.registrationOutPort.doDisconnection();
        this.clockOutPort.doDisconnection();
        for (NodeP2POutPort port : p2pOutPorts.values()) {
            if (port.connected()) {
                port.doDisconnection();
            }
        }
        super.finalise();
    }

    @Override
    public void uninstall() throws Exception {
        this.registrationOutPort.unpublishPort();
        this.registrationOutPort.destroyPort();
        this.clockOutPort.unpublishPort();
        this.clockOutPort.destroyPort();
        for (NodeP2POutPort port : p2pOutPorts.values()) {
            if (port.isPublished()) {
                port.unpublishPort();
                port.destroyPort();
            }
        }
        super.uninstall();
    }

    @Override
    public void ask4Connection(NodeInfoI neighbour) throws Exception {
        Direction dir = nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
        logMessage(nodeInfo.nodeIdentifier() + ": ask4Connection(requesting) <- " + neighbour.nodeIdentifier() + " dir: " + dir);
        Set<NodeInfoI> neighbours = processingNode.getNeighbours();
        PositionI position = nodeInfo.nodePosition();
        Optional<NodeInfoI> currentNeighbour = neighbours.stream().filter(cn -> position.directionFrom(cn.nodePosition()).equals(dir)).findFirst();
        if (currentNeighbour.isPresent()) {
            NodeInfoI nodeInfoVoisin = currentNeighbour.get();
            this.p2pOutPorts.get(dir).ask4Disconnection(this.nodeInfo);
            this.p2pOutPorts.get(dir).doDisconnection();
            neighbours.remove(nodeInfoVoisin);
        }
        this.p2pOutPorts.get(dir)
                        .doConnection(((BCM4JavaEndPointDescriptorI) neighbour.p2pEndPointInfo()).getInboundPortURI(),
                                      ConnectorNodeP2P.class.getCanonicalName());
        neighbours.add(neighbour);
        logMessage(nodeInfo.nodeIdentifier() + ": ask4Connection(done) <- " + neighbour.nodeIdentifier() + " dir: " + dir);
    }

    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        Direction dir = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
        logMessage(nodeInfo.nodeIdentifier() + ": ask4Disconnection(requesting) <- " + neighbour.nodeIdentifier() + " dir: " + dir);

        this.p2pOutPorts.get(dir).doDisconnection();
        processingNode.getNeighbours().remove(neighbour);
        this.getOwner().runTask(f -> {
            try {
                NodeInfoI newNeighbour = registrationOutPort.findNewNeighbour(nodeInfo, dir);
                if (newNeighbour != null && !newNeighbour.equals(neighbour) && !newNeighbour.equals(nodeInfo)) {
                    p2pOutPorts.get(dir).doConnection(newNeighbour.p2pEndPointInfo().toString(), new ConnectorNodeP2P());
                    p2pOutPorts.get(dir).ask4Connection(nodeInfo);
                    processingNode.getNeighbours().add(newNeighbour);
                    logMessage(nodeInfo.nodeIdentifier() + ": found new neighbor: " + newNeighbour.nodePosition() + " dir:" + dir);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        });

        logMessage(nodeInfo.nodeIdentifier() + ": ask4Disconnection(done) <- " + neighbour.nodeIdentifier() + " dir: " + dir);
    }


    @Override
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
                    p2pOutPorts.get(nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition()))
                               .execute(new RequestContinuation(request, execState.copy()));
                }
            }
        } else {
            for (Direction dir : execState.getDirections()) {
                if (this.p2pOutPorts.get(dir).connected()) {
                    p2pOutPorts.get(dir).execute(new RequestContinuation(request, execState.copyWithDirection(dir)));
                }
            }
        }
        return execState.getCurrentResult();
    }

    /**
     * Executes a continuation of a request.
     * Processes the continuation and returns the query result.
     *
     * @param reqCont the continuation of the request to execute
     * @return the query result
     * @throws Exception if an error occurs during execution
     */
    @Override
    public QueryResultI execute(RequestContinuationI reqCont) throws Exception {
        assert reqCont.getExecutionState() instanceof ExecutionState;
        if (requestAlreadyProcessed(reqCont.requestURI())) return null;
        this.addToProcessedRequests(reqCont.requestURI());

        ExecutionStateI execState = reqCont.getExecutionState();
        if (execState.isDirectional() && execState.noMoreHops()) return null;
        assert execState instanceof ExecutionState;

        execState.updateProcessingNode(this.processingNode);
        execState.addToCurrentResult(((Query) reqCont.getQueryCode()).eval(execState));
        Visualisation.addRequest(reqCont.requestURI(), this.nodeInfo.nodeIdentifier());

        if (execState.isFlooding()) {
            for (NodeInfoI neighbourInfo : processingNode.getNeighbours()) {
                if (execState.withinMaximalDistance(neighbourInfo.nodePosition())) {
                    p2pOutPorts.get(nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition()))
                               .execute(new RequestContinuation(reqCont, ((ExecutionState) execState).copy()));
                }
            }
        } else {
            execState.incrementHops();
            for (Direction dir : execState.getDirections()) {
                if (this.p2pOutPorts.get(dir).connected()) {
                    p2pOutPorts.get(dir).execute(new RequestContinuation(reqCont, ((ExecutionState) execState).copyWithDirection(dir)));
                }
            }
        }
        return execState.getCurrentResult();
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
                p2pOutPorts.get(dir).executeAsync(new RequestContinuation(request, execState.copy()));
            }
        } else {
            if (execState.noMoreHops()) sendBackToClient(request, execState.getCurrentResult());

            List<Direction> propagationDirections = getPropagationDirections(execState);
            if (propagationDirections.isEmpty()) {
                sendBackToClient(request, execState.getCurrentResult());
            } else {
                for (Direction dir : propagationDirections) {
                    p2pOutPorts.get(dir).executeAsync(new RequestContinuation(request, execState.copyWithDirection(dir)));
                }
            }
        }
    }

    /**
     * Executes a request continuation asynchronously.
     *
     * @param request the continuation of the request to execute
     * @throws Exception if an error occurs during execution
     */
    @Override
    public void executeAsync(RequestContinuationI request) throws Exception {
        assert request.getQueryCode() instanceof Query;
        ExecutionStateI execState = request.getExecutionState();
        assert execState instanceof ExecutionState;


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
                p2pOutPorts.get(nodeInfo.nodePosition().directionFrom(neighbourInfo.nodePosition()))
                           .executeAsync(new RequestContinuation(request, ((ExecutionState) execState).copy()));
            }
        } else {
            execState.incrementHops();
            if (execState.noMoreHops()) {
                sendBackToClient(request, execState.getCurrentResult());
            }

            List<Direction> propagationDirections = getPropagationDirections(execState);
            if (propagationDirections.isEmpty()) {
                sendBackToClient(request, execState.getCurrentResult());
            } else {
                for (Direction dir : propagationDirections) {
                    this.p2pOutPorts.get(dir).executeAsync(new RequestContinuation(request, ((ExecutionState) execState).copyWithDirection(dir)));
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
    protected void sendBackToClient(RequestI request, QueryResultI result) throws Exception {
        ConnectionInfoI connInfo = request.clientConnectionInfo();
        String portUri = this.nodeInfo.nodeIdentifier() + ":out:" + ((BCM4JavaEndPointDescriptorI) connInfo.endPointInfo()).getInboundPortURI();

        NodeReqResultOutPort port = new NodeReqResultOutPort(portUri, this.getOwner());
        port.publishPort();
        port.doConnection(((BCM4JavaEndPointDescriptorI) connInfo.endPointInfo()).getInboundPortURI(), ConnectorNodeClient.class.getCanonicalName());
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
    protected List<Direction> getPropagationDirections(ExecutionStateI execState) throws Exception {
        List<Direction> connectedDirections = new ArrayList<>();
        for (Direction dir : execState.getDirections()) {
            if (this.p2pOutPorts.get(dir).connected()) {
                connectedDirections.add(dir);
            }
        }
        return connectedDirections;
    }

    /**
     * Checks if a request with the given URI has already been processed.
     *
     * @param requestUri the URI of the request to check
     * @return true if the request has already been processed, false otherwise
     */
    protected synchronized boolean requestAlreadyProcessed(String requestUri) {
        return processedRequests.contains(requestUri);
    }

    /**
     * Removes a processed request from the list of processed requests.
     * This is method is called after a timeout when a request has been processed.
     *
     * @param requestUri the URI of the request to remove
     */
    protected synchronized void removeProcessedRequest(String requestUri) {
        processedRequests.remove(requestUri);
    }

    /**
     * Adds a request URI to the list of processed requests.
     * After adding the request URI, a task is scheduled to remove the processed request after a timeout.
     *
     * @param requestUri the URI of the request to add to the processed requests list
     */
    protected synchronized void addToProcessedRequests(String requestUri) {
        this.processedRequests.add(requestUri);
        AbstractComponent.AbstractTask task = new AbstractComponent.AbstractTask(this.getPluginURI()) {
            @Override
            public void run() {
                removeProcessedRequest(requestUri);
            }
        };
        this.scheduleTaskOnComponent(task, 200, TimeUnit.MILLISECONDS);
    }

}
