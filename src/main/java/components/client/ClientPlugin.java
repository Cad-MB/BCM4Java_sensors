package components.client;

import components.client.inbound_ports.ClientReqResultInPort;
import components.client.outbound_ports.ClientLookupOutPort;
import cvm.TestsContainer;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import parsers.ClientParser;
import parsers.TestParser;
import sensor_network.BCM4JavaEndPointDescriptor;
import sensor_network.PortName;
import sensor_network.requests.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ClientPlugin
    extends AbstractPlugin
    implements ClientCI {

    protected final List<TestParser.Test> tests;
    protected final int frequency;
    protected final String clientId;
    protected final int endAfter;
    protected final TestsContainer testsContainer;
    protected final ArrayList<ClientParser.Target> targets;

    protected final Map<PortName, String> inboundPortUris;
    protected final Map<PortName, String> outboundPortUris;
    protected final BCM4JavaEndPointDescriptor endPointDescriptor;
    protected final Request.ConnectionInfo connInfo;
    protected final Map<String, QueryResultI> results;
    protected final Queue<String> onGoingRequests;

    protected ClientReqResultInPort reqResultInPort;
    protected ClientLookupOutPort lookupOutPort;
    protected ClocksServerOutboundPort clockOutPort;

    public ClientPlugin(
        ClientParser.Client clientData,
        Map<PortName, String> inboundPortUris,
        Map<PortName, String> outboundPortUris,
        List<TestParser.Test> tests
    ) {
        super();
        this.inboundPortUris = inboundPortUris;
        this.outboundPortUris = outboundPortUris;
        this.tests = tests;
        this.frequency = clientData.frequency;
        this.clientId = clientData.id;
        this.targets = clientData.targets;
        this.endAfter = clientData.endAfter;
        this.testsContainer = new TestsContainer();

        this.endPointDescriptor = new BCM4JavaEndPointDescriptor(inboundPortUris.get(PortName.REQUEST_RESULT), RequestResultCI.class);
        this.connInfo = new Request.ConnectionInfo(clientId, endPointDescriptor);

        this.results = new ConcurrentHashMap<>();
        this.onGoingRequests = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void installOn(ComponentI owner) throws Exception {
        super.installOn(owner);

        this.addOfferedInterface(RequestResultCI.class);

        this.addRequiredInterface(ClientCI.class);
        this.addRequiredInterface(LookupCI.class);
        this.addRequiredInterface(ClocksServerCI.class);
    }

    @Override
    public void initialise() throws Exception {
        super.initialise();
        this.reqResultInPort = new ClientReqResultInPort(inboundPortUris.get(PortName.REQUEST_RESULT), this.getOwner(), this.getPluginURI());
        this.reqResultInPort.publishPort();

        this.lookupOutPort = new ClientLookupOutPort(outboundPortUris.get(PortName.LOOKUP), this.getOwner());
        this.lookupOutPort.publishPort();

        this.clockOutPort = new ClocksServerOutboundPort(outboundPortUris.get(PortName.CLOCK), this.getOwner());
        this.clockOutPort.publishPort();
    }

    @Override
    public void finalise() throws Exception {
        super.finalise();
    }

    @Override
    public void uninstall() throws Exception {
        super.uninstall();
    }

    @Override
    public QueryResultI sendRequest(RequestI r) throws Exception {
        return null;
    }

    @Override
    public void sendAsyncRequest(RequestI req) throws Exception {

    }

}
