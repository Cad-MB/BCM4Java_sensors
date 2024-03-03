package ast.cont;

import ast.dirs.Dirs;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import requests.ExecutionState;

/**
 * This class represents a directional continuation in the abstract syntax tree (AST) of queries.
 * It extends the Cont class and implements the eval method to configure the execution state with the specified directions.
 */
public class DCont
    implements Cont {

    /**
     * The directions to follow in the directional continuation.
     */
    Dirs dirs;
    /**
     * The maximum number of hops allowed in the directional continuation.
     */
    int nbSauts;

    /**
     * Constructor for the DCont class.
     *
     * @param dirs    The directions to follow in the directional continuation.
     * @param nbSauts The maximum number of hops allowed in the directional continuation.
     */
    public DCont(Dirs dirs, int nbSauts) {
        this.dirs = dirs;
        this.nbSauts = nbSauts;
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
        es.setDirectional(true);
        es.setNbHops(nbSauts);
        es.setDirections(dirs.eval(executionState));
        return null;
    }

}
