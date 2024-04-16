package ast.query;

import ast.bexp.BExp;
import ast.cont.Cont;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import sensor_network.requests.QueryResult;

import java.util.Objects;

/**
 * This class represents a boolean query in the abstract syntax tree (AST).
 * It extends the abstract Query class and implements the eval method to evaluate the query.
 */
public class BQuery
    implements Query {

    /**
     * The boolean expression to evaluate.
     */
    protected BExp bExp;
    /**
     * The continuation of the query.
     */
    protected Cont cont;

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
        cont.eval(executionState);
        QueryResult result = new QueryResult(true);
        if (bExp.eval(executionState)) {
            result.addPositiveNode(executionState.getProcessingNode().getNodeIdentifier());
        }
        return result;
    }

    @Override
    public String queryString() {
        return "bool " + bExp.queryString() + " " + cont.queryString();
    }

    @Override
    public String toString() {
        return "BQuery{bExp=" + bExp + ", cont=" + cont + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BQuery bQuery = (BQuery) o;
        return Objects.equals(bExp, bQuery.bExp) && Objects.equals(cont, bQuery.cont);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bExp, cont);
    }

}
