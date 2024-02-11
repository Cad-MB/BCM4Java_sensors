package components;

import ast.query.Query;
import components.interfaces.NetworkNodeCI;
import executionState.ExecutionState;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import node.Node;
import ports.NetworkNodeInboundPort;
import sensor.SensorData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

@OfferedInterfaces(offered = {NetworkNodeCI.class})
public class NetworkNode
        extends AbstractComponent
{
    public static final String NNIP_URI = "nnip-uri";
    protected NetworkNodeInboundPort nnip;
    protected NetworkNode() throws Exception {
        super(1,0);
        this.nnip = new NetworkNodeInboundPort(NNIP_URI, this);
        this.nnip.publishPort();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.nnip.unpublishPort();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.shutdown();
    }

    public ArrayList<String> evaluation (Query q) throws Exception
    {
        Node currentNode = new Node(100, "node1");
        currentNode.sensors = new HashMap<>(); // Initialize the sensors map
        SensorData<Double> sensorData = new SensorData<>(currentNode.getNodeIdentifier(), "sensor1", 100d, Instant.now());
        currentNode.sensors.put("sensor1", sensorData);

        ExecutionState state = new ExecutionState(currentNode);

        return q.eval(state).positiveSensorNodes();
    }

}

