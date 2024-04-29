package components.node;

import components.ConnectorNodeP2P;
import components.node.inbound_ports.NodeP2PInPort;
import components.node.inbound_ports.NodeRequestingInPort;
import components.node.outbound_ports.NodeP2POutPort;
import components.node.outbound_ports.NodeRegistrationOutPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.cps.sensor_network.interfaces.*;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import parsers.NodeParser;
import sensor_network.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NodePlugin
    extends AbstractPlugin
    implements SensorNodeP2PImplI, RequestingCI {

    private final NodeInfo nodeInfo;
    private final Long endDelay;
    private final Long startDelay;
    private final Long sensorUpdateDelay;
    private final Map<String, SensorData<Float>> sensorDataMap;
    private final Map<String, Float> sensorDataUpdateMap;
    private final Map<PortName, String> inboundPortUris;
    private final Map<PortName, String> outboundPortUris;

    private NodeRequestingInPort requestingInPort;
    private NodeRegistrationOutPort registrationOutPort;
    private HashMap<Direction, NodeP2POutPort> portsForP2P;
    private NodeP2PInPort p2PInPort;
    private ClocksServerOutboundPort clockOutPort;
    private ProcessingNodeI processingNode;

    public NodePlugin(
        NodeParser.Node nodeData,
        Map<PortName, String> inboundPortUris,
        Map<PortName, String> outboundPortUris
    ) {
        super();
        this.nodeInfo = new NodeInfo(nodeData.range, nodeData.id, new Position(nodeData.position.x, nodeData.position.y));
        this.endDelay = nodeData.endAfter;
        this.startDelay = nodeData.startAfter;
        this.sensorUpdateDelay = nodeData.sensorUpdateDelay;
        this.sensorDataMap = nodeData.sensors
            .stream()
            .map(sd -> new SensorData<>(nodeInfo.nodeIdentifier(), sd.id, sd.value, Instant.now()))
            .collect(Collectors.toMap(SensorData::getSensorIdentifier, data -> data));
        this.sensorDataUpdateMap = nodeData.sensors
            .stream()
            .collect(Collectors.toMap(sensor -> sensor.id, sensor -> sensor.toAdd));
        this.inboundPortUris = inboundPortUris;
        this.outboundPortUris = outboundPortUris;
    }

    @Override
    public void installOn(ComponentI owner) throws Exception {
        super.installOn(owner);

        this.addOfferedInterface(RequestingCI.class);
        this.addOfferedInterface(SensorNodeP2PCI.class);

        this.addRequiredInterface(RegistrationCI.class);
        this.addRequiredInterface(SensorNodeP2PCI.class);
        this.addRequiredInterface(RequestingCI.class);

        this.nodeInfo.setEndPointInfo(new BCM4JavaEndPointDescriptor(inboundPortUris.get(PortName.REQUESTING), RequestingCI.class));
        this.nodeInfo.setP2pEndPointInfo(new BCM4JavaEndPointDescriptor(inboundPortUris.get(PortName.P2P), SensorNodeP2PCI.class));
    }

    @Override
    public void initialise() throws Exception {
        super.initialise();
        this.requestingInPort = new NodeRequestingInPort(inboundPortUris.get(PortName.REQUESTING), this.getOwner(), this.getPluginURI());
        this.requestingInPort.publishPort();

        this.registrationOutPort = new NodeRegistrationOutPort(outboundPortUris.get(PortName.REGISTRATION), this.getOwner());
        this.registrationOutPort.publishPort();

        this.portsForP2P = new HashMap<>();
        for (Direction dir : Direction.values()) {
            NodeP2POutPort port = new NodeP2POutPort(outboundPortUris.get(PortName.P2P) + "-" + dir.name(), this.getOwner());
            port.publishPort();
            portsForP2P.put(dir, port);
        }

        this.p2PInPort = new NodeP2PInPort(inboundPortUris.get(PortName.P2P), this.getOwner(), this.getPluginURI());
        this.p2PInPort.publishPort();

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
    public void ask4Connection(NodeInfoI neighbour) throws Exception {
        Direction dir = nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
        logMessage(nodeInfo.nodeIdentifier() + ": ask4Connection(requesting) <- " + neighbour.nodeIdentifier() + " dir: " + dir);
        Set<NodeInfoI> neighbours = processingNode.getNeighbours();
        PositionI position = nodeInfo.nodePosition();
        Optional<NodeInfoI> currentNeighbour = neighbours.stream().filter(cn -> position.directionFrom(cn.nodePosition()).equals(dir)).findFirst();
        if (currentNeighbour.isPresent()) {
            NodeInfoI nodeInfoVoisin = currentNeighbour.get();
            this.portsForP2P.get(dir).ask4Disconnection(this.nodeInfo);
            this.portsForP2P.get(dir).doDisconnection();
            neighbours.remove(nodeInfoVoisin);
        }
        this.portsForP2P.get(dir).doConnection(neighbour.p2pEndPointInfo().toString(), components.ConnectorNodeP2P.class.getCanonicalName());
        neighbours.add(neighbour);
        logMessage(nodeInfo.nodeIdentifier() + ": ask4Connection(done) <- " + neighbour.nodeIdentifier() + " dir: " + dir);
    }

    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        Direction dir = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
        logMessage(nodeInfo.nodeIdentifier() + ": ask4Disconnection(requesting) <- " + neighbour.nodeIdentifier() + " dir: " + dir);

        this.portsForP2P.get(dir).doDisconnection();
        processingNode.getNeighbours().remove(neighbour);
        AbstractComponent.AbstractTask t = new AbstractComponent.AbstractTask(this.getPluginURI()) {
            @Override
            public void run() {
                try {
                    NodeInfoI newNeighbour = registrationOutPort.findNewNeighbour(nodeInfo, dir);
                    if (newNeighbour != null && !newNeighbour.equals(neighbour) && !newNeighbour.equals(nodeInfo)) {
                        portsForP2P.get(dir).doConnection(newNeighbour.p2pEndPointInfo().toString(), new ConnectorNodeP2P());
                        portsForP2P.get(dir).ask4Connection(nodeInfo);
                        processingNode.getNeighbours().add(newNeighbour);
                        logMessage(nodeInfo.nodeIdentifier() + ": found new neighbor: " + newNeighbour.nodePosition() + " dir:" + dir);
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        t.setOwnerReference(this.getOwner());
        this.runTaskOnComponent(t);

        logMessage(nodeInfo.nodeIdentifier() + ": ask4Disconnection(done) <- " + neighbour.nodeIdentifier() + " dir: " + dir);
    }

    @Override
    public QueryResultI execute(RequestContinuationI i) throws Exception {
        return null;
    }

    @Override
    public void executeAsync(RequestContinuationI i) throws Exception {

    }

    @Override
    public QueryResultI execute(RequestI i) throws Exception {
        return null;
    }

    @Override
    public void executeAsync(RequestI i) throws Exception {

    }

}
