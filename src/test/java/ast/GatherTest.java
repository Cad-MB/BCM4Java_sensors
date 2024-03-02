package ast;

import ast.gather.FGather;
import ast.gather.RGather;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.ExecutionState;
import requests.Position;
import requests.ProcessingNode;
import requests.SensorData;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GatherTest {

    ExecutionState es;
    SensorData<Boolean> sensorData1 = new SensorData<>(
        "test-node",
        "test-sensor1",
        true,
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
        Set<SensorDataI> sensorData = new HashSet<>();
        sensorData.add(sensorData1);
        sensorData.add(sensorData2);
        ProcessingNode pn = new ProcessingNode("test-node", new Position(0, 0), new HashSet<>(), sensorData);
        es = new ExecutionState(pn);
    }

    @Test
    void fGather() {
        HashMap<String, SensorDataI> evaled = new FGather("test-sensor1").eval(es);

        Collection<SensorDataI> expected = new HashSet<>();
        expected.add(sensorData1);

        assertEquals(expected, new HashSet<>(evaled.values()));
    }

    @Test
    void rGather() throws Exception {
        HashMap<String, SensorDataI> evaled = new RGather("test-sensor1", new FGather("test-sensor2")).eval(es);

        Set<SensorDataI> expected = new HashSet<>();
        expected.add(sensorData1);
        expected.add(sensorData2);

        assertEquals(expected, new HashSet<>(evaled.values()));
    }

}
