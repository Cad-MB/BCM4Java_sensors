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
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.utils.aclocks.*;
import logger.CustomTraceWindow;
import requests.QueryResult;
import requests.Request;

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

@OfferedInterfaces(offered={ ClientNodeInCI.class })
@RequiredInterfaces(required={ ClientNodeOutCI.class, LookupCI.class, ClocksServerCI.class })
public class Client
    extends AbstractComponent {

    private final ArrayList<String> nodeIds;
    private final ArrayList<Query> queries;
    private final int frequency;
    private final long startDelay;
    protected ClientPortForNode clientPortForNode;
    protected ClientPortForRegistry clientPortForRegistry;
    protected ClocksServerOutboundPort clockPort;
    protected static int nth = 0;
    protected final String clientId;

    private final Map<String, QueryResultI> results;
    private final Queue<String> onGoingRequests;

    /**
     * Constructs a new client component.
     * Initializes the ports for node and registry communication and toggles logging and tracing.
     *
     * @throws Exception if an error occurs during initialization
     */
    protected Client(String id, ArrayList<String> nodeIds, ArrayList<Query> queries, int frequency) throws Exception {
        super(1, 2);
        this.frequency = frequency;
        this.clientId = id;
        this.nodeIds = nodeIds;
        this.queries = queries;
        this.clientPortForNode = new ClientPortForNode(uri(OUTBOUND_URI.NODE), this);
        this.clientPortForNode.publishPort();
        this.clientPortForRegistry = new ClientPortForRegistry(uri(OUTBOUND_URI.REGISTRY), this);
        this.clientPortForRegistry.publishPort();
        this.clockPort = new ClocksServerOutboundPort(uri(OUTBOUND_URI.CLOCK), this);
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

        this.doPortConnection(
            uri(OUTBOUND_URI.CLOCK),
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
                    ConnectionInfoI node = this.clientPortForRegistry.findByIdentifier(nodeIds.get(finalI));
                    this.doPortConnection(
                        uri(OUTBOUND_URI.NODE),
                        node.endPointInfo().toString(),
                        ConnectorClientNode.class.getCanonicalName()
                    );
                    asyncQuery(node);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }, delay + i, TimeUnit.NANOSECONDS);
        }
    }

    int requestCounter = 0;

    private void asyncQuery(ConnectionInfoI node) {
        // todo set correct frequency and initialDelay
        this.scheduleTaskAtFixedRate(a -> {
            Query query = this.queries.get(getRandomNumber(queries.size()));
            Request.ConnectionInfo connInfo = new Request.ConnectionInfo(node.nodeIdentifier(), node.endPointInfo());
            Request request = new Request(clientId + "-" + requestCounter++, query, connInfo, false);

            try {
                this.clientPortForNode.sendAsyncRequest(request);
                this.onGoingRequests.add(request.requestURI());
                this.scheduleTask(e -> {
                    this.onGoingRequests.remove(request.requestURI());
                    System.out.println("result: " + this.results.get(request.requestURI()));
                }, 2000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
            this.logMessage("query= " + query.queryString());
        }, 5000, frequency, TimeUnit.MILLISECONDS);
    }

    private void query(ConnectionInfoI node) {
        // todo set correct frequency and initialDelay
        this.scheduleTaskAtFixedRate(a -> {
            Query query = this.queries.get(getRandomNumber(queries.size()));
            Request.ConnectionInfo connInfo = new Request.ConnectionInfo(node.nodeIdentifier(), node.endPointInfo());
            Request request = new Request(clientId + "-" + requestCounter++, query, connInfo, false);

            QueryResultI result = null;
            try {
                result = this.clientPortForNode.sendRequest(request);
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
        System.out.println("called accept query result");
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
            if (this.isPortConnected(uri(outboundUri))) {
                this.doPortDisconnection(uri(outboundUri));
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
            this.clientPortForNode.unpublishPort();
            this.clientPortForRegistry.unpublishPort();
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

    private String uri(OUTBOUND_URI uri) {
        return uri.uri + "-" + clientId;
    }

    /**
     * Enumerates the outbound URIs for the client component.
     */
    public enum OUTBOUND_URI {
        NODE("cop-uri"),
        REGISTRY("client-vers-registre-uri"),
        CLOCK("client-clock-uri");

        public final String uri;

        OUTBOUND_URI(String uri) {
            this.uri = uri;
        }
    }

    public int getRandomNumber(int max) {
        return (int) ((Math.random() * max));
    }

}
