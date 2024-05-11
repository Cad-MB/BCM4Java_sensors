package components.client;

import ast.query.Query;
import components.ConnectorClientNode;
import components.ConnectorClientRegistry;
import components.client.inbound_ports.ClientReqResultInPort;
import components.client.outbound_ports.ClientLookupOutPort;
import components.client.outbound_ports.ClientOutPort;
import components.registry.Registry;
import cvm.CVM;
import cvm.TestsContainer;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.cps.sensor_network.interfaces.*;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.utils.aclocks.*;
import parsers.ClientParser;
import parsers.TestParser;
import parsers.query.QueryParser;
import sensor_network.BCM4JavaEndPointDescriptor;
import sensor_network.PortName;
import sensor_network.requests.QueryResult;
import sensor_network.requests.Request;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ClientPlugin
    extends AbstractPlugin {

    protected final List<TestParser.Test> tests;
    protected AcceleratedClock clock;

    protected int requestCounter = 0;
    protected final int frequency;
    protected final String clientId;
    protected final int endAfter;
    protected final TestsContainer testsContainer;
    protected final ArrayList<ClientParser.Target> targets;

    protected final Map<PortName, String> inboundPortUris;
    protected final Map<PortName, String> outboundPortUris;
    protected final BCM4JavaEndPointDescriptor endPointDescriptor;
    protected final Request.ConnectionInfo connInfo;
    protected final Map<String, QueryResultI> results;
    protected final Queue<String> onGoingRequests;

    protected ClientReqResultInPort reqResultInPort;
    protected ClientLookupOutPort lookupOutPort;
    protected ClocksServerOutboundPort clockOutPort;
    protected int requestTimeout;

    public ClientPlugin(
        ClientParser.Client clientData,
        Map<PortName, String> inboundPortUris,
        Map<PortName, String> outboundPortUris,
        List<TestParser.Test> tests
    ) {
        super();
        this.inboundPortUris = inboundPortUris;
        this.outboundPortUris = outboundPortUris;
        this.tests = tests;
        this.frequency = clientData.frequency;
        this.clientId = clientData.id;
        this.targets = clientData.targets;
        this.endAfter = clientData.endAfter;
        this.requestTimeout = clientData.requestTimeout;
        this.testsContainer = new TestsContainer();

        this.endPointDescriptor = new BCM4JavaEndPointDescriptor(inboundPortUris.get(PortName.REQUEST_RESULT), RequestResultCI.class);
        this.connInfo = new Request.ConnectionInfo(clientId, endPointDescriptor);

        this.results = new ConcurrentHashMap<>();
        this.onGoingRequests = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void installOn(ComponentI owner) throws Exception {
        super.installOn(owner);

        this.addOfferedInterface(RequestResultCI.class);
        this.addRequiredInterface(ClientCI.class);
        this.addRequiredInterface(LookupCI.class);
        this.addRequiredInterface(ClocksServerCI.class);
    }

    @Override
    public void initialise() throws Exception {
        super.initialise();

        this.reqResultInPort = new ClientReqResultInPort(inboundPortUris.get(PortName.REQUEST_RESULT), this.getOwner(), this.getPluginURI());
        this.reqResultInPort.publishPort();

        this.lookupOutPort = new ClientLookupOutPort(outboundPortUris.get(PortName.LOOKUP), this.getOwner());
        this.lookupOutPort.publishPort();

        this.clockOutPort = new ClocksServerOutboundPort(outboundPortUris.get(PortName.CLOCK), this.getOwner());
        this.clockOutPort.publishPort();
    }

    @Override
    public void finalise() throws Exception {
        super.finalise();

        testsContainer.recap();
        this.lookupOutPort.doDisconnection();
        this.clockOutPort.doDisconnection();
    }

    @Override
    public void uninstall() throws Exception {
        super.uninstall();

        testsContainer.recap();

        this.lookupOutPort.unpublishPort();
        this.clockOutPort.unpublishPort();

        this.lookupOutPort.destroyPort();
        this.clockOutPort.destroyPort();

        this.removeRequiredInterface(ClientCI.class);
        this.removeRequiredInterface(LookupCI.class);
        this.removeRequiredInterface(ClocksServerCI.class);
    }

    private void sendRequestTask(ClientParser.Target target, Query query) {
        Request request = new Request(clientId + "-" + requestCounter, query, connInfo, target.async);
        requestCounter++;
        try {
            ConnectionInfoI nodeConn = this.lookupOutPort.findByIdentifier(target.nodeId);
            if (nodeConn == null) {
                System.err.println("Registry did not send info for node: " + target.nodeId);
                logMessage("Registry did not send info for node: " + target.nodeId);
                return;
            }
            this.onGoingRequests.add(request.requestURI());
            ClientOutPort port = new ClientOutPort(target.targetPort, this.getOwner());
            port.publishPort();
            port.doConnection(((BCM4JavaEndPointDescriptorI) nodeConn.endPointInfo()).getInboundPortURI(),
                              ConnectorClientNode.class.getCanonicalName());

            logMessage(String.format("(request) uri=%s, async=%s, query=%s", request.requestURI(), target.async, query.queryString()));
            if (target.async) {
                port.sendAsyncRequest(request);
                long delayToWaitForRes = clock.nanoDelayUntilInstant(clock.currentInstant().plusMillis(this.requestTimeout));
                this.getOwner().scheduleTask(f -> {
                    String formattedMessage = String.format("(response) uri=%s, result=%s", request.requestURI(), results.get(request.requestURI()).toString());
                    logMessage(formattedMessage);
                    System.out.println(formattedMessage);
                    onGoingRequests.remove(request.requestURI());
                }, delayToWaitForRes, TimeUnit.NANOSECONDS);
            } else {
                QueryResultI res = port.sendRequest(request);
                logMessage("result: " + res);
            }

            port.doDisconnection();
            port.unpublishPort();
            port.destroyPort();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

    }

    public void acceptQueryResult(String reqUri, QueryResultI queryResult) {
        if (!this.onGoingRequests.contains(reqUri)) return;
        QueryResultI currRes = this.results.getOrDefault(reqUri, new QueryResult(queryResult.isBooleanRequest()));
        if (currRes.isBooleanRequest()) {
            for (String positiveSensorNode : queryResult.positiveSensorNodes()) {
                if (!currRes.positiveSensorNodes().contains(positiveSensorNode)) {
                    currRes.positiveSensorNodes().add(positiveSensorNode);
                }
            }
        } else {
            currRes.gatheredSensorsValues().addAll(queryResult.gatheredSensorsValues());
        }
        this.results.put(reqUri, currRes);
    }

    private void testTask(TestParser.Test test) {
        long testDelay = clock.nanoDelayUntilInstant(clock.currentInstant().plusSeconds(test.afterDelay));
        this.getOwner().scheduleTask(f -> {
            if (test.isBoolean) {
                List<String> actualResults = this.results.get(test.requestId).positiveSensorNodes();
                Collections.sort(actualResults);
                Collections.sort(test.nodeIds);
                if (actualResults.equals(test.nodeIds)) {
                    testsContainer.addOkResult(test.name);
                } else {
                    List<Object> nodeIdObjects = new ArrayList<>(test.nodeIds);
                    List<Object> actualResultObjects = new ArrayList<>(actualResults);
                    testsContainer.addFailResult(test.name, nodeIdObjects, actualResultObjects);
                }
            } else {
                List<SensorDataI> actualResults = this.results.get(test.requestId).gatheredSensorsValues();
                List<TestParser.GatherResult> mappedActualResults = actualResults.stream().map(tr -> {
                    TestParser.GatherResult result = new TestParser.GatherResult();
                    result.nodeId = tr.getNodeIdentifier();
                    result.sensorId = tr.getSensorIdentifier();
                    result.value = (double) tr.getValue();
                    return result;
                }).sorted().collect(Collectors.toList());

                Collections.sort(test.gatherResults);

                if (mappedActualResults.equals(test.gatherResults)) {
                    testsContainer.addOkResult(test.name);
                } else {
                    List<Object> gatherResultObjects = new ArrayList<>(test.gatherResults);
                    List<Object> actualResultObjects = new ArrayList<>(mappedActualResults);
                    testsContainer.addFailResult(test.name, gatherResultObjects, actualResultObjects);
                }
            }
        }, testDelay, TimeUnit.NANOSECONDS);

    }

    protected void run() throws Exception {
        Thread.currentThread().setName(clientId);

        this.clockOutPort.doConnection(ClocksServer.STANDARD_INBOUNDPORT_URI, new ClocksServerConnector());
        clock = this.clockOutPort.getClock(CVM.CLOCK_URI);
        clock.waitUntilStart();
        Instant baseInstant = clock.currentInstant();

        this.lookupOutPort.doConnection(Registry.INBOUND_URI.LOOKUP.uri, new ConnectorClientRegistry());

        targets.forEach(target -> {
            long initialDelay = clock.nanoDelayUntilInstant(baseInstant.plusSeconds(target.initialDelay));
            long frequencyDelay = clock.nanoDelayUntilInstant(baseInstant.plusSeconds(frequency));
            long endDelay = clock.nanoDelayUntilInstant(baseInstant.plusSeconds(endAfter));

            Query query = QueryParser.parseQuery(target.query).parsed();
            AbstractComponent.AbstractTask task = new AbstractComponent.AbstractTask(this.getPluginURI()) {
                @Override
                public void run() {
                    sendRequestTask(target, query);
                }
            };
            ScheduledFuture<?> future = this.scheduleTaskAtFixedRateOnComponent(task, initialDelay, frequencyDelay, TimeUnit.NANOSECONDS);
            this.getOwner().scheduleTask(f -> future.cancel(true), endDelay, TimeUnit.NANOSECONDS);
        });

        tests.forEach(this::testTask);
    }

}
