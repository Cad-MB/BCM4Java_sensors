package ast;

import ast.gather.FGather;
import ast.gather.RGather;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sensor_network.Position;
import sensor_network.SensorData;
import sensor_network.requests.ExecutionState;
import sensor_network.requests.ProcessingNode;

import java.time.Instant;
import java.util.*;

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
        Map<String, SensorDataI> sensorData = new HashMap<>();
        sensorData.put(sensorData1.getSensorIdentifier(), sensorData1);
        sensorData.put(sensorData2.getSensorIdentifier(), sensorData2);
        ProcessingNode pn = new ProcessingNode("test-node", new Position(0, 0), new HashSet<>(), sensorData);
        es = new ExecutionState(pn);
    }

    @Test
    void fGather() {
        List<SensorDataI> evaled = new FGather("test-sensor1").eval(es);

        Collection<SensorDataI> expected = new HashSet<>();
        expected.add(sensorData1);

        assertEquals(expected, new HashSet<>(evaled));
    }

    @Test
    void rGather() throws Exception {
        List<SensorDataI> evaled = new RGather("test-sensor1", new FGather("test-sensor2")).eval(es);

        Set<SensorDataI> expected = new HashSet<>();
        expected.add(sensorData1);
        expected.add(sensorData2);

        assertEquals(expected, new HashSet<>(evaled));
    }

}
