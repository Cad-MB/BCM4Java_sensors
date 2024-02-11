package ast.gather;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

import java.util.HashMap;

/**
 * Cette classe abstraite représente une opération de collecte de données dans l'arbre de syntaxe abstraite (AST).
 * Elle étend l'interface QueryI, utilisée pour représenter les requêtes dans le réseau de capteurs,
 * ainsi que l'interface ASTNode pour la manipulation des nœuds de l'arbre de syntaxe abstraite.
 * Cette classe fournit une base commune pour d'autres classes d'opérations de collecte spécifiques.
 * @param <K> Le type de la clé de la map résultante.
 * @param <V> Le type de la valeur de la map résultante.
 */
public abstract class Gather<K, V> implements QueryI, ASTNode<HashMap<K, SensorDataI>> {
    // Les détails de l'implémentation spécifique seront fournis dans les sous-classes.
}
