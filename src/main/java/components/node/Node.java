package components.node;

import ast.query.Query;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import requests.ExecutionState;
import requests.NodeInfo;
import requests.SensorData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

@OfferedInterfaces(offered = {NodeCI.class})
public class Node
        extends AbstractComponent
{
    public static final String NNIP_URI = "nnip-uri";
    protected NodeInboundPort nnip;
    protected NodeInfo currentNodeInfo;
    protected Node() throws Exception {
        super(1, 0);
        this.nnip = new NodeInboundPort(NNIP_URI, this);
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

    public ArrayList<String> evaluationB (Query q) throws Exception
    {
        return q.eval(new ExecutionState(currentNodeInfo)).positiveSensorNodes();
    }

    public ArrayList<SensorDataI> evaluationG (Query q) throws Exception
    {
        return q.eval(new ExecutionState(currentNodeInfo)).gatheredSensorsValues();
    }

}

