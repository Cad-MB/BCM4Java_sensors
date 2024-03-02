package ast;

import ast.bexp.*;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.ExecutionState;
import requests.Position;
import requests.ProcessingNode;
import requests.SensorData;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BExpTest {

    ExecutionState es;

    BExp b1 = (es) -> false;
    BExp b2 = (es) -> true;

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
    void andBExp() throws Exception {
        Boolean exp1 = new AndBExp(b1, b2).eval(es);
        Boolean exp2 = new AndBExp(b1, b1).eval(es);
        Boolean exp3 = new AndBExp(b2, b2).eval(es);

        assertFalse(exp1);
        assertFalse(exp2);
        assertTrue(exp3);
    }

    @Test
    void cExpBExp() throws Exception {
        Boolean exp1 = new CExpBExp((es) -> false).eval(es);
        Boolean exp2 = new CExpBExp((es) -> true).eval(es);

        assertFalse(exp1);
        assertTrue(exp2);
    }

    @Test
    void notBExp() throws Exception {
        Boolean exp1 = new NotBExp(b1).eval(es);
        Boolean exp2 = new NotBExp(b2).eval(es);

        assertTrue(exp1);
        assertFalse(exp2);
    }

    @Test
    void orBExp() throws Exception {
        Boolean exp1 = new OrBExp(b1, b2).eval(es);
        Boolean exp2 = new OrBExp(b1, b1).eval(es);
        Boolean exp3 = new OrBExp(b2, b2).eval(es);

        assertTrue(exp1);
        assertFalse(exp2);
        assertTrue(exp3);
    }

    @Test
    void sBExp() throws Exception {
        Boolean exp1 = new SBExp("test-sensor1").eval(es);
        Boolean exp2 = new SBExp("test-sensor2").eval(es);

        assertTrue(exp1);
        assertFalse(exp2);
    }

}