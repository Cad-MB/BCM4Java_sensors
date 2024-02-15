package components.client;

import ast.bexp.CExpBExp;
import ast.cexp.EqCExp;
import ast.cont.DCont;
import ast.cont.ECont;
import ast.dirs.FDirs;
import ast.gather.FGather;
import ast.query.BQuery;
import ast.query.GQuery;
import ast.query.Query;
import ast.rand.CRand;
import ast.rand.SRand;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.ArrayList;

@RequiredInterfaces(required = {ClientCI.class})
public class Client extends AbstractComponent {
    protected Client() throws Exception {
        super(1, 0);
        this.cop = new ClientOutboundPort(OUTBOUND_URI.NODE.uri, this);
        this.cop.publishPort();
        this.toggleLogging();
        this.toggleTracing();
    }

    protected ClientOutboundPort cop;

    @Override
    public void execute() throws Exception {
        super.execute();
        gQuery();
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.doPortDisconnection(OUTBOUND_URI.NODE.uri);
        super.finalise();
    }

    void bQuery() throws Exception {
        Query query = new BQuery(
            new CExpBExp(new EqCExp(new SRand("sensor1"), new CRand(100))),
            new ECont());
        ArrayList<String> result = this.cop.sendRequestB(query);
        this.logMessage("binary query result= " + result);
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.cop.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    void gQuery() throws Exception {
        Query gQuery = new GQuery(new FGather("sensor1"), new DCont(new FDirs(Direction.NE), 1));
        ArrayList<SensorDataI> resultG = this.cop.sendRequestG(gQuery);
        this.logMessage("gather query result= " + resultG);
    }

    public enum OUTBOUND_URI {
        NODE("cop-uri");

        public final String uri;

        OUTBOUND_URI(String uri) {
            this.uri = uri;
        }
    }
}
