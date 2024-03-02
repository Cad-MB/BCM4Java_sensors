package ast.dirs;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

import java.util.Set;

/**
 * Cette classe abstraite représente une liste de directions dans l'arbre de syntaxe abstraite (AST) des requêtes.
 * Elle étend la classe QueryI et implémente la classe ASTNode pour représenter un ensemble de directions.
 */
public interface Dirs
    extends QueryI, ASTNode<Set<Direction>> {
}
