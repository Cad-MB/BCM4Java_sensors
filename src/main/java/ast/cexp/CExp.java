package ast.cexp;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

/**
 * This abstract class represents a comparison expression in the abstract syntax tree (AST).
 * It extends the QueryI interface, used to represent queries in the sensor network,
 * as well as the ASTNode interface for manipulating nodes in the abstract syntax tree.
 * This class provides a common base for other specific comparison expression classes.
 */
public interface CExp
    extends QueryI, ASTNode<Boolean> {
    // The specific implementation details will be provided in the subclasses.
}
