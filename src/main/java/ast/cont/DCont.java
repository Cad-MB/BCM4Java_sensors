package ast.cont;

import ast.dirs.Dirs;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import requests.ExecutionState;

import java.util.Objects;

/**
 * This class represents a directional continuation in the abstract syntax tree (AST) of queries.
 * It extends the Cont class and implements the eval method to configure the execution state with the specified directions.
 */
public class DCont
    implements Cont {

    /**
     * The directions to follow in the directional continuation.
     */
    protected Dirs dirs;
    /**
     * The maximum number of hops allowed in the directional continuation.
     */
    protected int nbHops;

    /**
     * Constructor for the DCont class.
     *
     * @param dirs   The directions to follow in the directional continuation.
     * @param nbHops The maximum number of hops allowed in the directional continuation.
     */
    public DCont(Dirs dirs, int nbHops) {
        this.dirs = dirs;
        this.nbHops = nbHops;
    }

    /**
     * Configures the execution state with the specified directions in the directional continuation.
     *
     * @param executionState The current execution state.
     * @return null because this method does not return any specific result.
     * @throws Exception If an error occurs while configuring the execution state.
     */
    @Override
    public Void eval(ExecutionStateI executionState) throws Exception {
        assert executionState instanceof ExecutionState;
        ExecutionState es = (ExecutionState) executionState;
        // todo maybe not update nb hops every time
        es.setDirectionalState(nbHops, dirs.eval(executionState));
        return null;
    }

    @Override
    public String queryString() {
        return "(dir " + dirs.queryString() + " " + nbHops + ')';
    }

    @Override
    public String toString() {
        return "DCont{dirs=" + dirs + ", nbSauts=" + nbHops + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DCont cont = (DCont) o;
        return nbHops == cont.nbHops && Objects.equals(dirs, cont.dirs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dirs, nbHops);
    }

}
