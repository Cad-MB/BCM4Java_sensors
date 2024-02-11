package components;

import ast.bexp.CExpBExp;
import ast.cexp.EqCExp;
import ast.cont.ECont;
import ast.query.BQuery;
import ast.query.Query;
import ast.rand.CRand;
import ast.rand.SRand;
import components.interfaces.ClientCI;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import ports.ClientOutboundPort;

@RequiredInterfaces(required = {ClientCI.class})
public class Client
extends AbstractComponent
{
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
                new CExpBExp(new EqCExp(new SRand("sensor1"), new CRand(101))),
                new ECont());
        String result = this.cop.sendRequest(bQuery);
        this.logMessage("query result= " + result);
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
            throw new RuntimeException(e);
        }
        super.shutdown();
    }
}
