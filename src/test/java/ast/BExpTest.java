package ast;

import ast.bexp.*;
import ast.cexp.CExp;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parsers.query.QueryParser;
import sensor_network.Position;
import sensor_network.SensorData;
import sensor_network.requests.ExecutionState;
import sensor_network.requests.ProcessingNode;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BExpTest {

    ExecutionState es;

    BExp b1;
    BExp b2;

    @BeforeEach
    void init() {
        b1 = QueryParser.parseBExp("(100 = 0)").parsed();
        b2 = QueryParser.parseBExp("(100 = 100)").parsed();

        Map<String, SensorDataI> sensorData = new HashMap<>();
        sensorData.put("test-sensor1", new SensorData<>(
            "test-node",
            "test-sensor1",
            true,
            Instant.now()
        ));
        sensorData.put("test-sensor2", new SensorData<>(
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
        CExp cExp1 = QueryParser.parseCExp("100 = 20").parsed();
        CExp cExp2 = QueryParser.parseCExp("100 = 100").parsed();
        Boolean exp1 = new CExpBExp(cExp1).eval(es);
        Boolean exp2 = new CExpBExp(cExp2).eval(es);

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