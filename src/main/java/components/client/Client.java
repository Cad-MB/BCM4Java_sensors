package components.client;

import ast.query.Query;
import components.ConnectorClientNode;
import cvm.CVM;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.utils.aclocks.*;
import logger.CustomTraceWindow;
import requests.Request;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a client component in the sensor network system.
 * It communicates with the registry to discover nodes and sends queries to them periodically.
 * The client component is responsible for gathering data from the sensor nodes.
 */
@RequiredInterfaces(required={ ClientCI.class, LookupCI.class, ClocksServerCI.class })
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

    /**
     * Constructs a new client component.
     * Initializes the ports for node and registry communication and toggles logging and tracing.
     *
     * @throws Exception if an error occurs during initialization
     */
    protected Client(ArrayList<String> nodeIds, ArrayList<Query> queries, int frequency) throws Exception {
        super(1, 1);
        this.frequency = frequency;
        clientId = "client" + nth;
        this.nodeIds = nodeIds;
        this.queries = queries;
        this.clientPortForNode = new ClientPortForNode(uri(OUTBOUND_URI.NODE), this);
        this.clientPortForNode.publishPort();
        this.clientPortForRegistry = new ClientPortForRegistry(uri(OUTBOUND_URI.REGISTRY), this);
        this.clientPortForRegistry.publishPort();
        this.clockPort = new ClocksServerOutboundPort(uri(OUTBOUND_URI.CLOCK), this);
        this.clockPort.publishPort();


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
        this.logMessage("CLIENT");
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
            // int finalI = i;
            this.scheduleTask(f -> {
                try {
                    ConnectionInfoI node = this.clientPortForRegistry.findByIdentifier(this.nodeIds.get(0));
                    this.doPortConnection(
                        uri(OUTBOUND_URI.NODE),
                        node.endPointInfo().toString(),
                        ConnectorClientNode.class.getCanonicalName());
                    query(node);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, delay + i, TimeUnit.NANOSECONDS);
        }
    }

    private void query(ConnectionInfoI node) {
        this.scheduleTaskAtFixedRate(a -> {
            System.out.println("Client.query");
            Request request = new Request(
                "test" + nth, this.queries.get(getRandomNumber(queries.size())),
                new Request.ConnectionInfo(node.nodeIdentifier(), node.endPointInfo()),
                false);

            System.out.println("Client.query");
            QueryResultI resultG3;
            try {
                System.out.println(this.clientPortForNode.isRemotelyConnected());
                resultG3 = this.clientPortForNode.sendRequest(request);
                System.out.println("Client.query");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.logMessage("gather query result= " + resultG3);
            System.out.println("gather query result = " + resultG3);
        }, 5000, frequency, TimeUnit.MILLISECONDS);
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
            if (this.isPortConnected(outboundUri.uri)) {
                this.doPortDisconnection(outboundUri.uri);
            }
        }
        this.doPortDisconnection(this.clockPort.getPortURI());
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

    public static String uri(OUTBOUND_URI uri, int n) {
        return uri.uri + "-client" + n;
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
