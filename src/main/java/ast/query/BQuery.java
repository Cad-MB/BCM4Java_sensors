package ast.query;

import ast.bexp.BExp;
import ast.cont.Cont;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import requests.QueryResult;

/**
 * This class represents a boolean query in the abstract syntax tree (AST).
 * It extends the abstract Query class and implements the eval method to evaluate the query.
 */
public class BQuery
    implements Query {

    /**
     * The boolean expression to evaluate.
     */
    BExp bExp;
    /**
     * The continuation of the query.
     */
    Cont cont;

    /**
     * Constructor for the BQuery class.
     *
     * @param bExp The boolean expression to evaluate.
     * @param cont The continuation of the query.
     */
    public BQuery(BExp bExp, Cont cont) {
        this.bExp = bExp;
        this.cont = cont;
    }

    /**
     * Evaluates the boolean query.
     *
     * @param executionState The current execution state.
     * @return The result of the boolean query.
     * @throws Exception If an error occurs during the evaluation of the continuation.
     */
    @Override
    public QueryResultI eval(ExecutionStateI executionState) throws Exception {
        cont.eval(executionState); // Evaluation of the continuation
        QueryResult result = new QueryResult(true); // Creating a QueryResult object initialized to true
        // If the boolean expression is true, add the identifier of the positive node to the result
        if (bExp.eval(executionState))
            result.addPositiveNode(executionState.getProcessingNode().getNodeIdentifier());
        return result; // Return the query result
    }

}
