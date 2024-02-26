package ast.base;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

/**
 * Cette classe abstraite représente une base dans l'arbre de syntaxe abstraite (AST) des requêtes.
 * Elle étend la classe QueryI et implémente la classe ASTNode pour représenter une position.
 */
public abstract class Base
    implements QueryI, ASTNode<PositionI> {
}
