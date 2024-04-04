package ast;

import ast.cexp.*;
import ast.rand.CRand;
import ast.rand.Rand;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sensor_network.Position;
import sensor_network.SensorData;
import sensor_network.requests.ExecutionState;
import sensor_network.requests.ProcessingNode;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CExpTest {

    ExecutionState es;

    Rand rand1 = new CRand(200d);
    Rand rand2 = new CRand(10d);

    @BeforeEach
    void init() {
        Set<SensorDataI> sensorData = new HashSet<>();
        sensorData.add(new SensorData<>(
            "test-node",
            "test-sensor1",
            true,
            Instant.now()
        ));
        sensorData.add(new SensorData<>(
            "test-node",
            "test-sensor2",
            false,
            Instant.now()
        ));
        ProcessingNode pn = new ProcessingNode("test-node", new Position(0, 0), new HashSet<>(), sensorData);
        es = new ExecutionState(pn);

    }

    @Test
    void eqCExp() throws Exception {
        Boolean evaled = new EqCExp(rand1, rand2).eval(es);
        assertFalse(evaled);
    }

    @Test
    void gCExp() throws Exception {
        Boolean evaled = new GCExp(rand1, rand2).eval(es);
        assertTrue(evaled);
    }

    @Test
    void gEqCExp() throws Exception {
        Boolean evaled = new GeqCExp(rand1, rand2).eval(es);
        assertTrue(evaled);
    }

    @Test
    void lCExp() throws Exception {
        Boolean evaled = new LCExp(rand1, rand2).eval(es);
        assertFalse(evaled);
    }

    @Test
    void lEqCExp() throws Exception {
        Boolean evaled = new LeqCExp(rand1, rand2).eval(es);
        assertFalse(evaled);
    }

}
