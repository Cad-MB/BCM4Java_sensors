package ast;

import ast.base.ABase;
import ast.base.RBase;
import ast.cont.FCont;
import ast.query.BQuery;
import ast.query.GQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.ExecutionState;
import requests.Position;
import requests.ProcessingNode;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseTest {

    ExecutionState es;

    @BeforeEach
    void initialize() {
        es = new ExecutionState(new ProcessingNode("test-node", new Position(0, 0), new HashSet<>(), new HashSet<>()));
    }

    @Test
    void aBase() throws Exception {
        Position pos = new Position(200, 200);
        ABase base = new ABase(pos);
        new BQuery((es) -> false, new FCont(base, 200)).eval(es);

        assertTrue(es.withinMaximalDistance(new Position(100, 100)));
        assertFalse(es.withinMaximalDistance(new Position(401, 500)));
    }

    @Test
    void rBase() throws Exception {
        new GQuery(es -> new HashMap<>(), new FCont(new RBase(), 100)).eval(es);

        assertTrue(es.withinMaximalDistance(new Position(60, 60)));
        assertFalse(es.withinMaximalDistance(new Position(101, 101)));
    }

}