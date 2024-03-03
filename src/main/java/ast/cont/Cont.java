package ast.cont;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

/**
 * This abstract class represents a continuation in the abstract syntax tree (AST) of queries.
 * It extends the QueryI class and implements the ASTNode class.
 */
public interface Cont
    extends QueryI, ASTNode<Void> {
}
