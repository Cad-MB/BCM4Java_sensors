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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    protected final Map<String, Instant> requests;

    protected ClientReqResultInPort reqResultInPort;
    protected ClientLookupOutPort lookupOutPort;
    protected ClocksServerOutboundPort clockOutPort;
    protected int requestTimeout;

    /**
     * Constructs a ClientPlugin with the specified client data, inbound port URIs,
     * outbound port URIs, and list of tests.
     *
     * @param clientData The data for the client including frequencies, timeouts, and target information.
     * @param inboundPortUris A map of port names to inbound port URIs.
     * @param outboundPortUris A map of port names to outbound port URIs.
     * @param tests A list of tests to be performed by this client.
     */
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
        this.requests = new ConcurrentHashMap<>();
    }

    /**
     * Installs this plugin on the specified component by adding necessary interfaces.
     *
     * @param owner The component on which the plugin will be installed.
     * @throws Exception If there is an error during the installation.
     */
    @Override
    public void installOn(ComponentI owner) throws Exception {
        super.installOn(owner);

        this.addOfferedInterface(RequestResultCI.class);
        this.addRequiredInterface(ClientCI.class);
        this.addRequiredInterface(LookupCI.class);
        this.addRequiredInterface(ClocksServerCI.class);
    }

    /**
     * Initializes the plugin by setting up necessary ports and preparing for operation.
     *
     * @throws Exception If there is an error during the initialization.
     */
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

    /**
     * Finalises the plugin, performing cleanup and result summarisation.
     * @throws Exception if an error occurs during finalisation.
     */
    @Override
    public void finalise() throws Exception {
        super.finalise();

        testsContainer.recap();
        System.out.println("pourcentage après timeout: " + (100f * nbRequestFailed) / nbRequestReceived);
        System.out.println("nb total requests : " + nbRequestReceived);
        this.lookupOutPort.doDisconnection();
        this.clockOutPort.doDisconnection();
    }

    /**
     * Uninstalls the plugin from the component, removing interfaces and ports.
     * @throws Exception if an error occurs during uninstallation.
     */
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

    /**
     * Sends a request to a target node using the specified query.
     * @param target the target to which the request will be sent.
     * @param query the query to be sent as part of the request.
     */
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
            ClientOutPort port = new ClientOutPort(target.targetPort, this.getOwner());
            port.publishPort();
            port.doConnection(((BCM4JavaEndPointDescriptorI) nodeConn.endPointInfo()).getInboundPortURI(),
                              ConnectorClientNode.class.getCanonicalName());

            logMessage(String.format("(request) uri=%s, async=%s, query=%s", request.requestURI(), target.async, query.queryString()));
            if (target.async) {
                port.sendAsyncRequest(request);
                this.requests.put(request.requestURI(), clock.currentInstant().plusMillis(this.requestTimeout));
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

    long nbRequestReceived = 0;
    long nbRequestFailed = 0;

    /**
     * Accepts and processes a query result, checking for timeouts and updating results.
     * @param reqUri the URI of the request to which this result corresponds.
     * @param queryResult the result of the query to be processed.
     */
    public void acceptQueryResult(String reqUri, QueryResultI queryResult) {
        Instant timeoutInstant = this.requests.get(reqUri);
        Instant clockInstant = clock.currentInstant();
        // totalTemps += Duration.between(clockInstant, timeoutInstant).toMillis();
        nbRequestReceived++;
        if (clockInstant.isAfter(timeoutInstant)) {
            nbRequestFailed++;
            System.err.println("(response) for " + reqUri + " received after timeout");
            System.err.println("timeout offset :" + Duration.between(clockInstant, timeoutInstant));
            return;
        }
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
        logMessage(String.format("(response) uri=%s, result=%s", reqUri, currRes));
        // System.out.println("timeout offset :" + Duration.between(clockInstant, timeoutInstant));
        // System.out.printf("(response) uri=%s, result=%s%n", reqUri, currRes);
        // System.out.println("test");
    }

    /**
     * Schedules a test based on the provided test information.
     * @param test the test to be executed.
     */
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

    /**
     * Main execution method for the plugin, setting up the clock and processing targets.
     * @throws Exception if an error occurs during execution.
     */
    protected void run() throws Exception {
        Thread.currentThread().setName(clientId);

        this.clockOutPort.doConnection(ClocksServer.STANDARD_INBOUNDPORT_URI, new ClocksServerConnector());
        clock = this.clockOutPort.getClock(CVM.CLOCK_URI);
        clock.waitUntilStart();
        Instant baseInstant = clock.currentInstant();

        this.lookupOutPort.doConnection(Registry.INBOUND_URI.LOOKUP.uri, new ConnectorClientRegistry());
        System.out.println("ClientPlugin.run");
        System.out.println("roooooooo");
        targets.forEach(target -> {
            long initialDelay = clock.nanoDelayUntilInstant(baseInstant.plusSeconds(target.initialDelay));
            long frequencyDelay = clock.nanoDelayUntilInstant(clock.currentInstant().plusMillis(frequency));
            long endDelay = clock.nanoDelayUntilInstant(baseInstant.plusSeconds(endAfter));

            Query query = QueryParser.parseQuery(target.query).parsed();
            AbstractComponent.AbstractTask task = new AbstractComponent.AbstractTask(this.getPluginURI()) {
                @Override
                public void run() {
                    sendRequestTask(target, query);
                }
            };
            System.out.println(frequencyDelay);
            ScheduledFuture<?> future = this.scheduleTaskAtFixedRateOnComponent(task, initialDelay, frequencyDelay, TimeUnit.NANOSECONDS);
            this.getOwner().scheduleTask(f -> future.cancel(true), endDelay, TimeUnit.NANOSECONDS);
        });

        tests.forEach(this::testTask);
    }

}
