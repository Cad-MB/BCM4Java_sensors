package ast;

import ast.rand.CRand;
import ast.rand.SRand;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sensor_network.Position;
import sensor_network.SensorData;
import sensor_network.requests.ExecutionState;
import sensor_network.requests.ProcessingNode;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RandTest {

    ExecutionState es;
    SensorData<Double> sensorData1 = new SensorData<>(
        "test-node",
        "test-sensor1",
        200d,
        Instant.now()
    );
    SensorData<Double> sensorData2 = new SensorData<>(
        "test-node",
        "test-sensor2",
        10d,
        Instant.now()
    );

    @BeforeEach
    void init() {
        Map<String, SensorDataI> sensorData = new HashMap<>();
        sensorData.put(sensorData1.getSensorIdentifier(), sensorData1);
        sensorData.put(sensorData2.getSensorIdentifier(), sensorData2);
        ProcessingNode pn = new ProcessingNode("test-node", new Position(0, 0), new HashSet<>(), sensorData);
        es = new ExecutionState(pn);
    }


    @Test
    void cRand() throws Exception {
        Serializable eval = new CRand(200).eval(es);
        Double evaled = (Double) eval;
        assertEquals(200, evaled);
    }

    @Test
    void sRand() throws Exception {
        Serializable res1 = new SRand("test-sensor1").eval(es);
        Double evaled = (Double) res1;
        assertEquals(200, evaled);

        Serializable res2 = new SRand("test-sensor2").eval(es);
        evaled = (Double) res2;
        assertEquals(10d, evaled);
    }

}
