package components.client;

import ast.bexp.CExpBExp;
import ast.cexp.EqCExp;
import ast.cont.ECont;
import ast.gather.FGather;
import ast.query.BQuery;
import ast.query.GQuery;
import ast.query.Query;
import ast.rand.CRand;
import ast.rand.SRand;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.ArrayList;

@RequiredInterfaces(required = {ClientCI.class})
public class Client extends AbstractComponent {
    public static final String COP_URI = "cop-uri";
    protected ClientOutboundPort cop;

    protected Client() throws Exception {
        super(1, 0);
        this.cop = new ClientOutboundPort(COP_URI, this);
        this.cop.publishPort();
        this.toggleLogging();
        this.toggleTracing();
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        Query bQuery = new BQuery(
                new CExpBExp(new EqCExp(new SRand("sensor1"), new CRand(100))),
                new ECont());

        Query gQuery = new GQuery(new FGather("sensor1"), new ECont());

        ArrayList<String> resultB = this.cop.sendRequestB(bQuery);
        ArrayList<SensorDataI> resultG = this.cop.sendRequestG(gQuery);

        this.logMessage("binary query result= " + resultB);
        this.logMessage("gather query result= " + resultG);
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.doPortDisconnection(COP_URI);
        super.finalise();
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
}
