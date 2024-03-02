package ast;


import ast.dirs.FDirs;
import ast.dirs.RDirs;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.ExecutionState;
import requests.Position;
import requests.ProcessingNode;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DirsTest {

    ExecutionState es;

    @BeforeEach
    void init() {
        es = new ExecutionState(new ProcessingNode("test-node", new Position(0, 0), new HashSet<>(), new HashSet<>()));
    }

    @Test
    void fDirs() {
        Set<Direction> evaled = new FDirs(Direction.NE).eval(es);

        Set<Direction> expected = new HashSet<>();
        expected.add(Direction.NE);

        assertEquals(expected, evaled);
    }

    @Test
    void rDirs() throws Exception {
        Set<Direction> evaled =
            new RDirs(
                Direction.NE,
                new RDirs(
                    Direction.SE,
                    new FDirs(Direction.NW)
                )
            ).eval(es);

        Set<Direction> expected = new HashSet<>();
        expected.add(Direction.NE);
        expected.add(Direction.SE);
        expected.add(Direction.NW);

        assertEquals(expected, evaled);
    }

}
