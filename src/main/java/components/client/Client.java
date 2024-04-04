package components.client;

import ast.query.Query;
import components.ConnectorClientNode;
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
import sensor_network.EndPointInfo;
import sensor_network.requests.QueryResult;
import sensor_network.requests.Request;

import java.awt.*;
import java.time.Instant;
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

    protected final ArrayList<String> nodeIds;
    protected final ArrayList<Query> queries;
    protected final int frequency;
    protected final long startDelay;
    protected ClientPortFromNode portFromNode;
    protected ClientPortForNode portForNode;
    protected ClientPortForRegistry portForRegistry;
    protected ClocksServerOutboundPort clockPort;
    protected static int nth = 0;
    protected final String clientId;
    protected final Map<String, QueryResultI> results;
    protected final Queue<String> onGoingRequests;
    protected int requestCounter = 0;


    /**
     * Constructs a new client component.
     * Initializes the ports for node and registry communication and toggles logging and tracing.
     *
     * @throws Exception if an error occurs during initialization
     */
    protected Client(String id, ArrayList<String> nodeIds, ArrayList<Query> queries, int frequency) throws Exception {
        super(8, 8);
        this.frequency = frequency;
        this.clientId = id;
        this.nodeIds = nodeIds;
        this.queries = queries;
        this.portForNode = new ClientPortForNode(OUTBOUND_URI.NODE.of(clientId), this);
        this.portForNode.publishPort();
        this.portFromNode = new ClientPortFromNode(INBOUND_URI.NODE.of(clientId), this);
        this.portFromNode.publishPort();
        this.portForRegistry = new ClientPortForRegistry(OUTBOUND_URI.REGISTRY.of(clientId), this);
        this.portForRegistry.publishPort();
        this.clockPort = new ClocksServerOutboundPort(OUTBOUND_URI.CLOCK.of(clientId), this);
        this.clockPort.publishPort();

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
        this.startDelay = (6 + nth) * 60L;
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

        this.doPortConnection(
            OUTBOUND_URI.CLOCK.of(clientId),
            ClocksServer.STANDARD_INBOUNDPORT_URI,
            ClocksServerConnector.class.getCanonicalName()
        );
        AcceleratedClock clock = this.clockPort.getClock(CVM.CLOCK_URI);
        clock.waitUntilStart();
        Instant instantToWaitFor = clock.currentInstant().plusSeconds(startDelay);
        long delay = clock.nanoDelayUntilInstant(instantToWaitFor);

        for (int i = 0; i < nodeIds.size(); i++) {
            int finalI = i;
            this.scheduleTask(f -> {
                try {
                    ConnectionInfoI node = this.portForRegistry.findByIdentifier(nodeIds.get(finalI));
                    if (node == null) {
                        System.err.println("Registry did not send info for node: " + nodeIds.get(finalI));
                        return;
                    }
                    this.doPortConnection(
                        OUTBOUND_URI.NODE.of(clientId),
                        node.endPointInfo().toString(),
                        ConnectorClientNode.class.getCanonicalName()
                    );
                    asyncQuery();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }, delay + i, TimeUnit.NANOSECONDS);
        }
    }


    private void asyncQuery() {
        // todo set correct frequency and initialDelay
        this.scheduleTaskAtFixedRate(a -> {
            Query query = this.queries.get(getRandomNumber(queries.size()));
            EndPointInfo endPointDescriptor = new EndPointInfo(INBOUND_URI.NODE.of(clientId));
            Request.ConnectionInfo connInfo = new Request.ConnectionInfo(clientId, endPointDescriptor);
            Request request = new Request(clientId + "-" + requestCounter++, query, connInfo, true);

            try {
                this.portForNode.sendAsyncRequest(request);
                this.onGoingRequests.add(request.requestURI());
                this.scheduleTask(e -> System.out.println("result: " + this.results.get(request.requestURI())), 200, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
            this.logMessage("query= " + query.queryString());
        }, 5000, frequency, TimeUnit.MILLISECONDS);
    }

    private void query() {
        this.scheduleTaskAtFixedRate(a -> {
            Query query = this.queries.get(getRandomNumber(queries.size()));
            EndPointInfo endPointDescriptor = new EndPointInfo(INBOUND_URI.NODE.of(clientId));
            Request.ConnectionInfo connInfo = new Request.ConnectionInfo(clientId, endPointDescriptor);
            Request request = new Request(clientId + "-" + requestCounter++, query, connInfo, true);

            QueryResultI result = null;
            try {
                result = this.portForNode.sendRequest(request);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
            this.logMessage("query= " + query.queryString());
            this.logMessage("result= " + result);
            System.out.println("query result = " + result);
        }, 5000, frequency, TimeUnit.MILLISECONDS);
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
        for (OUTBOUND_URI outboundUri : OUTBOUND_URI.values()) {
            if (this.isPortConnected(outboundUri.of(clientId))) {
                this.doPortDisconnection(outboundUri.of(clientId));
            }
        }
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
            this.portForNode.unpublishPort();
            this.portForRegistry.unpublishPort();
            this.clockPort.unpublishPort();
            this.clockPort.destroyPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    public static String uri(OUTBOUND_URI uri, String clientId) {
        return uri.uri + "-" + clientId;
    }

    /**
     * Enumerates the outbound URIs for the client component.
     */
    public enum OUTBOUND_URI {
        NODE("cop-uri"),
        REGISTRY("client-vers-registre-uri"),
        CLOCK("client-clock-uri");

        private final String uri;

        public String of(String clientId) {
            return this.uri + "-" + clientId;
        }

        OUTBOUND_URI(String uri) {
            this.uri = uri;
        }
    }

    public enum INBOUND_URI {
        NODE("client-from-node");

        public final String uri;

        public String of(String clientId) {
            return this.uri + "-" + clientId;
        }

        INBOUND_URI(String uri) {
            this.uri = uri;
        }
    }

    public int getRandomNumber(int max) {
        return (int) ((Math.random() * max));
    }

}
