package ast.cont;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

/**
 * Cette classe abstraite représente une continuation dans l'arbre de syntaxe abstraite (AST) des requêtes.
 * Elle étend la classe QueryI et implémente la classe ASTNode.
 */
public abstract class Cont
    implements QueryI, ASTNode<Void> {
}
