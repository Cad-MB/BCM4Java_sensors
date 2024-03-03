package ast.dirs;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

import java.util.Set;

/**
 * This abstract class represents a list of directions in the abstract syntax tree (AST) of queries.
 * It extends the QueryI class and implements the ASTNode class to represent a set of directions.
 */
public interface Dirs
    extends QueryI, ASTNode<Set<Direction>> {
}
