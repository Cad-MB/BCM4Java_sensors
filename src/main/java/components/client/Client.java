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
import fr.sorbonne_u.utils.aclocks.*;
import requests.Request;

import java.util.concurrent.TimeUnit;

/**
 * This class represents a client component in the sensor network system.
 * It communicates with the registry to discover nodes and sends queries to them periodically.
 * The client component is responsible for gathering data from the sensor nodes.
 */
@RequiredInterfaces(required={ ClientCI.class, LookupCI.class, ClocksServerCI.class })
public class Client
    extends AbstractComponent {

    protected ClientPortForNode clientPortForNode;
    protected ClientPortForRegistry clientPortForRegistry;
    protected ClocksServerOutboundPort clockPort;

    /**
     * Constructs a new client component.
     * Initializes the ports for node and registry communication and toggles logging and tracing.
     *
     * @throws Exception if an error occurs during initialization
     */
    protected Client() throws Exception {
        super(1, 1);
        this.clientPortForNode = new ClientPortForNode(OUTBOUND_URI.NODE.uri, this);
        this.clientPortForNode.publishPort();
        this.clientPortForRegistry = new ClientPortForRegistry(OUTBOUND_URI.REGISTRY.uri, this);
        this.clientPortForRegistry.publishPort();
        this.clockPort = new ClocksServerOutboundPort(this);
        this.clockPort.publishPort();

        this.toggleLogging();
        this.toggleTracing();
        this.logMessage("CLIENT");
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
            this.clockPort.getPortURI(),
            ClocksServer.STANDARD_INBOUNDPORT_URI,
            ClocksServerConnector.class.getCanonicalName()
        );
        AcceleratedClock aClock = this.clockPort.getClock(this.clockPort.getPortURI());
        this.doPortDisconnection(this.clockPort.getPortURI());
        this.clockPort.unpublishPort();
        this.clockPort.destroyPort();

        aClock.waitUntilStart();

        ConnectionInfoI node = this.clientPortForRegistry.findByIdentifier("node1");

        this.doPortConnection(
            OUTBOUND_URI.NODE.uri,
            node.endPointInfo().toString(), ConnectorClientNode.class.getCanonicalName());


        this.scheduleTask(a -> {
            Query gQuery3 = new GQuery(new FGather("temp"), new FCont(new RBase(), 50));
            Request request3 = new Request("test3", gQuery3,
                                           new Request.ConnectionInfo(node.nodeIdentifier(), node.endPointInfo()),
                                           false);
            QueryResultI resultG3;
            try {
                resultG3 = this.clientPortForNode.sendRequest(request3);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.logMessage("gather query result= " + resultG3);
            System.out.println("gather query result = " + resultG3);
        }, 2, TimeUnit.SECONDS);

        // while (true) {
        //     Query gQuery3 = new GQuery(new FGather("temp"), new FCont(new RBase(), 50));
        //     Request request3 = new Request("test3", gQuery3,
        //                                    new Request.ConnectionInfo(node.nodeIdentifier(), node.endPointInfo()),
        //                                    false);
        //     QueryResultI resultG3 = this.clientPortForNode.sendRequest(request3);
        //     this.logMessage("gather query result= " + resultG3);
        //     System.out.println("gather query result = " + resultG3);
        // }
    }

    /**
     * Finalizes the client component.
     * Disconnects from ports and performs necessary cleanup.
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
