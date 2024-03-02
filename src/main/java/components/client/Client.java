package components.client;

import ast.base.ABase;
import ast.base.Base;
import ast.base.RBase;
import ast.cont.DCont;
import ast.cont.FCont;
import ast.dirs.FDirs;
import ast.gather.FGather;
import ast.query.GQuery;
import ast.query.Query;
import components.ConnectorClientNode;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import requests.Request;

@RequiredInterfaces(required={ ClientCI.class, LookupCI.class })
public class Client
    extends AbstractComponent {

    protected ClientPortForNode clientPortForNode;
    protected ClientPortForRegistry clientPortForRegistry;

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

    @Override
    public void execute() throws Exception {
        super.execute();

        Thread.sleep(2000);
        ConnectionInfoI node = this.clientPortForRegistry.findByIdentifier("node1");

        this.doPortConnection(
            OUTBOUND_URI.NODE.uri,
            node.endPointInfo().toString(), ConnectorClientNode.class.getCanonicalName());

        Query gQuery3 = new GQuery(new FGather("temp"), new FCont(new RBase(), 50));
        Request request3 = new Request("test3", gQuery3,
                                       new Request.ConnectionInfo(node.nodeIdentifier(), node.endPointInfo()), false);
        QueryResultI resultG3 = this.clientPortForNode.sendRequest(request3);
        this.logMessage("gather query result= " + resultG3);
        System.out.println("gather query result = " + resultG3);

        // Query gQuery1 = new GQuery(new FGather("temp"), new DCont(new FDirs(Direction.SE), 1));
        // Request request = new Request("test1", gQuery1,
        //                               new Request.ConnectionInfo(node.nodeIdentifier(), node.endPointInfo()), false);
        // QueryResultI resultG1 = this.clientPortForNode.sendRequest(request);
        // this.logMessage("gather query result= " + resultG1);
        // System.out.println("gather query result = " + resultG1);
        //
        // Thread.sleep(2000);
        //
        // Query gQuery2 = new GQuery(new FGather("temp"), new DCont(new FDirs(Direction.SE), 2));
        // Request request2 = new Request("test2", gQuery2,
        //                               new Request.ConnectionInfo(node.nodeIdentifier(), node.endPointInfo()), false);
        // QueryResultI resultG2 = this.clientPortForNode.sendRequest(request2);
        // this.logMessage("gather query result= " + resultG2);
        // System.out.println("gather query result = " + resultG2);

    }

    @Override
    public synchronized void finalise() throws Exception {
        for (OUTBOUND_URI outboundUri : OUTBOUND_URI.values()) {
            if (this.isPortConnected(outboundUri.uri)) {
                this.doPortDisconnection(outboundUri.uri);
            }
        }
        super.finalise();
    }

    void bQuery() throws Exception {
        // Query query = new BQuery(
        //     new CExpBExp(new EqCExp(new SRand("sensor1"), new CRand(100))),
        //     new ECont());
        // Request request = new Request("test", query, new Request.ConnectionInfo(), false);
        // QueryResultI result = this.clientPortForNode.sendRequest(request);
        // this.logMessage("binary query result= " + result);
    }

    void gQuery() throws Exception {
        // Query gQuery = new GQuery(new FGather("sensor1"), new DCont(new FDirs(Direction.NE), 1));
        // ArrayList<SensorDataI> resultG = this.clientPortForNode.sendRequest(gQuery);
        // this.logMessage("gather query result= " + resultG);
    }

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

    public enum OUTBOUND_URI {
        NODE("cop-uri"),
        REGISTRY("client-vers-registre-uri");

        public final String uri;

        OUTBOUND_URI(String uri) {
            this.uri = uri;
        }
    }

}
