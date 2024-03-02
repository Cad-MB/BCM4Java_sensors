package ast.query;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

/**
 * Cette classe abstraite représente une requête dans l'arbre de syntaxe abstraite (AST).
 * Elle implémente l'interface QueryI, utilisée pour représenter les requêtes dans le réseau de capteurs,
 * ainsi que l'interface ASTNode pour la manipulation des nœuds de l'arbre de syntaxe abstraite.
 * Cette classe fournit une base commune pour d'autres classes de requêtes spécifiques.
 */
public interface Query
    extends QueryI, ASTNode<QueryResultI> {
    // Les détails de l'implémentation spécifique seront fournis dans les sous-classes.
}
