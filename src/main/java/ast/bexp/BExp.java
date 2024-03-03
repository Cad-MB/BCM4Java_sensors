package ast.bexp;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

/**
 * This abstract class represents a boolean expression in the abstract syntax tree (AST).
 * It extends the QueryI interface, used to represent queries in the sensor network,
 * as well as the ASTNode interface for manipulation of nodes in the abstract syntax tree.
 * This class provides a common base for other specific boolean expression classes.
 */
public interface BExp
    extends QueryI, ASTNode<Boolean> {
    // Specific implementation details will be provided in subclasses.
}
