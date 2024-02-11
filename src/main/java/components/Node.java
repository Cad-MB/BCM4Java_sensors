package components;

import ast.query.Query;
import components.interfaces.NetworkNodeCI;
import executionState.ExecutionState;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import node.NodeInfo;
import ports.NetworkNodeInboundPort;
import sensor.SensorData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

@OfferedInterfaces(offered = {NetworkNodeCI.class})
public class Node
        extends AbstractComponent
{
    public static final String NNIP_URI = "nnip-uri";
    protected NetworkNodeInboundPort nnip;
    protected NodeInfo currentNodeInfo;
    protected Node() throws Exception {
        super(1, 0);
        this.nnip = new NetworkNodeInboundPort(NNIP_URI, this);
        this.nnip.publishPort();

        this.currentNodeInfo = new NodeInfo(100, "node1"); // Initialize currentNodeInfo
        this.currentNodeInfo.sensors = new HashMap<>();
        SensorData<Double> sensorData = new SensorData<>(this.currentNodeInfo.getNodeIdentifier(), "sensor1", 100d, Instant.now());
        this.currentNodeInfo.sensors.put("sensor1", sensorData);
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
        return q.eval(new ExecutionState(currentNodeInfo)).positiveSensorNodes();
    }

}

