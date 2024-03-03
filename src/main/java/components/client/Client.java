package components.client;

import ast.base.RBase;
import ast.cont.FCont;
import ast.gather.FGather;
import ast.query.GQuery;
import ast.query.Query;
import components.ConnectorClientNode;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import requests.Request;

/**
 * This class represents a client component in the sensor network system.
 * It communicates with the registry to discover nodes and sends queries to them periodically.
 * The client component is responsible for gathering data from the sensor nodes.
 */
@RequiredInterfaces(required={ ClientCI.class, LookupCI.class })
public class Client
    extends AbstractComponent {

    protected ClientPortForNode clientPortForNode;
    protected ClientPortForRegistry clientPortForRegistry;

    /**
     * Constructs a new client component.
     * Initializes the ports for node and registry communication and toggles logging and tracing.
     *
     * @throws Exception if an error occurs during initialization
     */
    protected Client() throws Exception {
        super(1, 0);
        this.clientPortForNode = new ClientPortForNode(OUTBOUND_URI.NODE.uri, this);
        this.clientPortForNode.publishPort();
        this.clientPortForRegistry = new ClientPortForRegistry(OUTBOUND_URI.REGISTRY.uri, this);
        this.clientPortForRegistry.publishPort();

        this.toggleLogging();
        this.toggleTracing();
        this.traceMessage("CLIENT\n");
    }

    /**
     * Executes the client component.
     * Periodically sends gather queries to sensor nodes and handles the results.
     * @throws Exception if an error occurs during execution
     */
    @Override
    public void execute() throws Exception {
        super.execute();

        Thread.sleep(2000);
        ConnectionInfoI node = this.clientPortForRegistry.findByIdentifier("node1");

        this.doPortConnection(
            OUTBOUND_URI.NODE.uri,
            node.endPointInfo().toString(), ConnectorClientNode.class.getCanonicalName());


        while (true) {
            Query gQuery3 = new GQuery(new FGather("temp"), new FCont(new RBase(), 50));
            Request request3 = new Request("test3", gQuery3,
                                           new Request.ConnectionInfo(node.nodeIdentifier(), node.endPointInfo()),
                                           false);
            QueryResultI resultG3 = this.clientPortForNode.sendRequest(request3);
            this.logMessage("gather query result= " + resultG3);
            System.out.println("gather query result = " + resultG3);
            Thread.sleep(2000);
        }
    }

    /**
     * Finalizes the client component.
     * Disconnects from ports and performs necessary cleanup.
     * @throws Exception if an error occurs during finalization
     */
    @Override
    public synchronized void finalise() throws Exception {
        for (OUTBOUND_URI outboundUri : OUTBOUND_URI.values()) {
            if (this.isPortConnected(outboundUri.uri)) {
                this.doPortDisconnection(outboundUri.uri);
            }
        }
        super.finalise();
    }

    /**
     * Shuts down the client component.
     * Unpublishes ports and shuts down gracefully.
     * @throws ComponentShutdownException if an error occurs during shutdown
     */
    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.clientPortForNode.unpublishPort();
            this.clientPortForRegistry.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    /**
     * Enumerates the outbound URIs for the client component.
     */
    public enum OUTBOUND_URI {
        NODE("cop-uri"),
        REGISTRY("client-vers-registre-uri");

        public final String uri;

        OUTBOUND_URI(String uri) {
            this.uri = uri;
        }
    }
}
