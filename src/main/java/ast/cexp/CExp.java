package ast.cexp;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

/**
 * Cette classe abstraite représente une expression de comparaison dans l'arbre de syntaxe abstraite (AST).
 * Elle étend l'interface QueryI, utilisée pour représenter les requêtes dans le réseau de capteurs,
 * ainsi que l'interface ASTNode pour la manipulation des nœuds de l'arbre de syntaxe abstraite.
 * Cette classe fournit une base commune pour d'autres classes d'expressions de comparaison spécifiques.
 */
public abstract class CExp
    implements QueryI, ASTNode<Boolean> {
    // Les détails de l'implémentation spécifique seront fournis dans les sous-classes.
}
