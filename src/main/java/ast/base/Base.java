package ast.base;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

/**
 * This abstract class represents a base in the abstract syntax tree (AST) of queries.
 * It extends the QueryI class and implements the ASTNode class to represent a position.
 */
public interface Base
    extends ASTNode<PositionI> {
}
