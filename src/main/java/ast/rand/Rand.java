package ast.rand;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

import java.io.Serializable;

/**
 * This class represents a random operand in the abstract syntax tree (AST).
 * It extends the QueryI class and implements the ASTNode class.
 */
public interface Rand
    extends QueryI, ASTNode<Serializable> {
}
