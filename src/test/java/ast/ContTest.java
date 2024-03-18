package ast;

import ast.base.ABase;
import ast.bexp.BExp;
import ast.bexp.CExpBExp;
import ast.cexp.EqCExp;
import ast.cont.DCont;
import ast.cont.ECont;
import ast.cont.FCont;
import ast.dirs.FDirs;
import ast.gather.FGather;
import ast.query.BQuery;
import ast.query.GQuery;
import ast.rand.CRand;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.ExecutionState;
import requests.Position;
import requests.ProcessingNode;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ContTest {

    ExecutionState es;

    @BeforeEach
    void init() {
        es = new ExecutionState(new ProcessingNode("test-node", new Position(0, 0), new HashSet<>(), new HashSet<>()));
    }


    @Test
    void eCont() throws Exception {
        // bQuery
        es.setNbHops(1);
        BExp bExp = new CExpBExp(new EqCExp(new CRand(100), new CRand(50))); // false
        new BQuery(bExp, new ECont()).eval(es);
        assertTrue(es.noMoreHops());
        assertFalse(es.isContinuationSet());

        // gQuery
        es.setNbHops(1);
        FGather gather = new FGather("test-node");
        new GQuery(gather, new ECont()).eval(es);
        assertTrue(es.noMoreHops());
        assertFalse(es.isContinuationSet());
    }

    @Test
    void dCont() throws Exception {

        es.setNbHops(0);
        DCont cont = new DCont(new FDirs(Direction.NE), 1);
        BExp bExp = new CExpBExp(new EqCExp(new CRand(100), new CRand(50))); // false
        new BQuery(bExp, cont).eval(es);

        Set<Direction> expectedDirections = new HashSet<>();
        expectedDirections.add(Direction.NE);

        assertEquals(expectedDirections, es.getDirections());
        assertTrue(es.isDirectional());
        assertFalse(es.noMoreHops());
    }


    @Test
    void fCont() throws Exception {
        double distance = 200;

        FGather gather = new FGather("test-node");
        new GQuery(gather, new FCont(new ABase(new Position(0, 0)), distance)).eval(es);
        es.setNbHops(1);

        assertTrue(es.withinMaximalDistance(new Position(50, 50)));
        assertFalse(es.withinMaximalDistance(new Position(201, 200)));
        assertFalse(es.noMoreHops());
        assertTrue(es.isFlooding());
    }

}