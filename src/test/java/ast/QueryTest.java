package ast;

import ast.bexp.CExpBExp;
import ast.cexp.EqCExp;
import ast.cont.ECont;
import ast.gather.FGather;
import ast.query.BQuery;
import ast.query.GQuery;
import ast.rand.CRand;
import ast.rand.SRand;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sensor_network.Position;
import sensor_network.SensorData;
import sensor_network.requests.ExecutionState;
import sensor_network.requests.ProcessingNode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueryTest {

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
    void bQuery() throws Exception {
        BQuery query = new BQuery(
            new CExpBExp(
                new EqCExp(
                    new SRand("test-sensor1"),
                    new CRand(200d)
                ))
            , new ECont());

        QueryResultI evaled = query.eval(es);
        ArrayList<String> expected = new ArrayList<>();
        expected.add("test-node");

        assertTrue(evaled.isBooleanRequest());
        assertFalse(evaled.isGatherRequest());
        assertEquals(expected, evaled.positiveSensorNodes());
    }

    @Test
    void gQuery() throws Exception {
        GQuery query = new GQuery(
            new FGather("test-sensor1")
            , new ECont());
        QueryResultI evaled = query.eval(es);

        ArrayList<SensorDataI> expected = new ArrayList<>();
        expected.add(sensorData1);
        assertTrue(evaled.isGatherRequest());
        assertFalse(evaled.isBooleanRequest());
        assertEquals(expected, evaled.gatheredSensorsValues());
    }

}