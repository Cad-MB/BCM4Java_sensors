package ast.rand;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

/**
 * Cette classe représente un opérande aléatoire dans l'arbre de syntaxe abstraite (AST).
 * Elle étend la classe QueryI et implémente la classe ASTNode.
 */
public abstract class Rand
    implements QueryI, ASTNode<Double> {
}
