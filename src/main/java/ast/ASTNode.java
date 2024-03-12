package ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * This interface represents a node in the abstract syntax tree (AST) of queries.
 * It defines an eval method that evaluates the node based on the provided execution state.
 *
 * @param <T> The type of result of evaluating the node.
 */
public interface ASTNode<T> {

    /**
     * Evaluates the node based on the provided execution state.
     *
     * @param executionState The current execution state.
     * @return The result of evaluating the node.
     * @throws Exception if an error occurs during evaluation.
     */
    T eval(ExecutionStateI executionState) throws Exception;

    String queryString();
}
