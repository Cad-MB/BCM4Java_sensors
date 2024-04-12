package components.client;

import ast.query.Query;
import components.ConnectorClientNode;
import components.ConnectorClientRegistry;
import components.client.inbound_ports.ClientReqResultInPort;
import components.client.outbound_ports.ClientLookupOutPort;
import components.client.outbound_ports.ClientNodeOutPort;
import components.registry.Registry;
import cvm.CVM;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.utils.aclocks.*;
import logger.CustomTraceWindow;
import parsers.ClientParser;
import parsers.query.QueryParser;
import sensor_network.BCM4JavaEndPointDescriptor;
import sensor_network.PortName;
import sensor_network.requests.QueryResult;
import sensor_network.requests.Request;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a client component in the sensor network system.
 * It communicates with the registry to discover nodes and sends queries to them periodically.
 * The client component is responsible for gathering data from the sensor nodes.
 */
@OfferedInterfaces(offered={ RequestResultCI.class })
@RequiredInterfaces(required={ ClientNodeOutCI.class, LookupCI.class, ClocksServerCI.class })
public class Client
    extends AbstractComponent {

    protected static int nth = 0;

    protected final ArrayList<ClientParser.Target> targets;
    protected final int frequency;
    protected final Request.ConnectionInfo connInfo;
    protected final String clientId;
    protected final Map<String, QueryResultI> results;
    protected final Queue<String> onGoingRequests;
    protected final BCM4JavaEndPointDescriptor endPointDescriptor;
    protected int requestCounter = 0;

    protected ClientReqResultInPort reqResultInPort;
    protected ClientLookupOutPort lookupOutPort;
    protected ClocksServerOutboundPort clockOutPort;

    /**
     * Constructs a new client component.
     * Initializes the ports for node and registry communication and toggles logging and tracing.
     *
     * @throws Exception if an error occurs during initialization
     */
    protected Client(
        ClientParser.Client clientData,
        Map<PortName, String> inboundPortUris,
        Map<PortName, String> outboundPortUris
    ) throws Exception {
        super(8, 8);
        this.frequency = clientData.frequency;
        this.clientId = clientData.id;
        this.targets = clientData.targets;
        this.reqResultInPort = new ClientReqResultInPort(inboundPortUris.get(PortName.REQUEST_RESULT), this);
        this.reqResultInPort.publishPort();
        this.lookupOutPort = new ClientLookupOutPort(outboundPortUris.get(PortName.LOOKUP), this);
        this.lookupOutPort.publishPort();
        this.clockOutPort = new ClocksServerOutboundPort(outboundPortUris.get(PortName.CLOCK), this);
        this.clockOutPort.publishPort();

        this.endPointDescriptor = new BCM4JavaEndPointDescriptor(inboundPortUris.get(PortName.REQUEST_RESULT), RequestResultCI.class);
        this.connInfo = new Request.ConnectionInfo(clientId, endPointDescriptor);

        this.results = new ConcurrentHashMap<>();
        this.onGoingRequests = new ConcurrentLinkedDeque<>();


        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        CustomTraceWindow tracerWindow = new CustomTraceWindow(
            "Client",
            0, 0,
            screenSize.width / 2, screenSize.height / 5,
            nth, 4
        );
        tracerWindow.setBackgroundColor(Color.decode("#ef5350"));
        tracerWindow.setForegroundColor(Color.WHITE);
        setTracer(tracerWindow);

        this.toggleLogging();
        this.toggleTracing();
        this.logMessage(clientId);
        nth++;
    }

    /**
     * Executes the client component.
     * Periodically sends gather queries to sensor nodes and handles the results.
     *
     * @throws Exception if an error occurs during execution
     */
    @Override
    public void execute() throws Exception {
        super.execute();
        Thread.currentThread().setName(clientId);

        this.clockOutPort.doConnection(ClocksServer.STANDARD_INBOUNDPORT_URI, new ClocksServerConnector());
        AcceleratedClock clock = this.clockOutPort.getClock(CVM.CLOCK_URI);
        clock.waitUntilStart();

        this.lookupOutPort.doConnection(Registry.INBOUND_URI.LOOKUP.uri, new ConnectorClientRegistry());

        targets.forEach(target -> {
            long initialDelay = clock.nanoDelayUntilInstant(clock.currentInstant().plusSeconds(target.initialDelay));
            long frequencyDelay = clock.nanoDelayUntilInstant(clock.currentInstant().plusSeconds(frequency));

            Query query = QueryParser.parseQuery(target.query).parsed();
            this.scheduleTaskAtFixedRate(f -> {
                Request request = new Request(clientId + "-" + requestCounter++, query, connInfo, target.async);
                try {
                    ConnectionInfoI nodeConn = this.lookupOutPort.findByIdentifier(target.nodeId);
                    if (nodeConn == null) {
                        System.err.println("Registry did not send info for node: " + target.nodeId);
                        logMessage("Registry did not send info for node: " + target.nodeId);
                        return;
                    }
                    ClientNodeOutPort port = new ClientNodeOutPort(target.targetPort, this);
                    port.publishPort();
                    port.doConnection(nodeConn.endPointInfo().toString(), new ConnectorClientNode());

                    this.logMessage(String.format("uri=%s, async=%s, query=%s", request.requestURI(), target.async, query.queryString()));
                    if (target.async) {
                        port.sendAsyncRequest(request);
                        long delayToWaitForRes = clock.nanoDelayUntilInstant(clock.currentInstant().plusSeconds(20));
                        this.scheduleTask(e -> {
                            String formattedMessage = String.format("uri=%s, result=%s", request.requestURI(), this.results.get(request.requestURI()));
                            this.logMessage(formattedMessage);
                            System.out.println(formattedMessage);
                            this.onGoingRequests.remove(request.requestURI());
                        }, delayToWaitForRes, TimeUnit.NANOSECONDS);
                    } else {
                        QueryResultI res = port.sendRequest(request);
                        this.logMessage("result: " + res);
                    }

                    port.doDisconnection();
                    port.unpublishPort();
                    port.destroyPort();
                    this.onGoingRequests.add(request.requestURI());
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }, initialDelay, frequencyDelay, TimeUnit.NANOSECONDS);
        });
    }

    public synchronized void acceptQueryResult(String reqUri, QueryResultI queryResult) {
        if (!this.onGoingRequests.contains(reqUri)) return;
        QueryResultI currRes = this.results.getOrDefault(reqUri, new QueryResult(queryResult.isBooleanRequest()));
        if (currRes.isBooleanRequest()) {
            currRes.positiveSensorNodes().addAll(queryResult.positiveSensorNodes());
        } else {
            currRes.gatheredSensorsValues().addAll(queryResult.gatheredSensorsValues());
        }
        results.put(reqUri, currRes);
    }


    /**
     * Finalizes the client component.
     * Disconnects from ports and performs necessary cleanups.
     *
     * @throws Exception if an error occurs during finalization
     */
    @Override
    public synchronized void finalise() throws Exception {
        this.lookupOutPort.doDisconnection();
        this.clockOutPort.doDisconnection();
        super.finalise();
    }

    /**
     * Shuts down the client component.
     * Unpublishes ports and shuts down gracefully.
     *
     * @throws ComponentShutdownException if an error occurs during shutdown
     */
    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.lookupOutPort.unpublishPort();
            this.lookupOutPort.destroyPort();

            this.clockOutPort.unpublishPort();
            this.clockOutPort.destroyPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

}
