package ast.bexp;

import ast.ASTNode;

/**
 * This abstract class represents a boolean expression in the abstract syntax tree (AST).
 * It extends the QueryI interface, used to represent queries in the sensor network,
 * as well as the ASTNode interface for manipulation of nodes in the abstract syntax tree.
 * This class provides a common base for other specific boolean expression classes.
 */
public interface BExp
    extends ASTNode<Boolean> {
}
