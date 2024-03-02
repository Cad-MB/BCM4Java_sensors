package ast;

import ast.cont.DCont;
import ast.cont.ECont;
import ast.cont.FCont;
import ast.query.BQuery;
import ast.query.GQuery;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.ExecutionState;
import requests.Position;
import requests.ProcessingNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ContTest {

    ExecutionState es;

    @BeforeEach
    void init() {
        es = new ExecutionState(new ProcessingNode("test-cont", new Position(0, 0), new HashSet<>(), new HashSet<>()));
    }


    @Test
    void eCont() throws Exception {
        // bQuery
        es.setNbHops(1);
        new BQuery(es -> false, new ECont()).eval(es);
        assertTrue(es.noMoreHops());
        assertFalse(es.isContinuationSet());

        // gQuery
        es.setNbHops(1);
        new GQuery(es -> new HashMap<>(), new ECont()).eval(es);
        assertTrue(es.noMoreHops());
        assertFalse(es.isContinuationSet());
    }

    @Test
    void dCont() throws Exception {
        Set<Direction> queryDirections = new HashSet<>();
        queryDirections.add(Direction.NE);

        es.setNbHops(0);
        DCont cont = new DCont((es) -> queryDirections, 1);
        new BQuery(es -> false, cont).eval(es);

        Set<Direction> expectedDirections = new HashSet<>();
        expectedDirections.add(Direction.NE);

        assertEquals(expectedDirections, es.getDirections());
        assertTrue(es.isDirectional());
        assertFalse(es.noMoreHops());
    }


    @Test
    void fCont() throws Exception {
        double distance = 200;

        new GQuery(es -> new HashMap<>(), new FCont((es) -> null, distance)).eval(es);
        es.setNbHops(1);

        assertTrue(es.withinMaximalDistance(new Position(50, 50)));
        assertFalse(es.withinMaximalDistance(new Position(201, 200)));
        assertFalse(es.noMoreHops());
        assertTrue(es.isFlooding());
    }

}