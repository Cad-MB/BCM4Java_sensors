package ast.dirs;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;

import java.util.Set;

/**
 * This abstract class represents a list of directions in the abstract syntax tree (AST) of queries.
 * It extends ASTNode interface to represent a set of directions.
 */
public interface Dirs
    extends ASTNode<Set<Direction>> {
}
