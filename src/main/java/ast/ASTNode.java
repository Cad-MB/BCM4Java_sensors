package ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * Cette interface représente un nœud dans l'arbre de syntaxe abstraite (AST) des requêtes.
 * Elle définit une méthode eval qui permet d'évaluer le nœud en fonction de l'état d'exécution fourni.
 *
 * @param <T> Le type de résultat de l'évaluation du nœud.
 */
public interface ASTNode<T> {
    /**
     * Évalue le nœud en fonction de l'état d'exécution fourni.
     *
     * @param executionState L'état d'exécution actuel.
     * @return Le résultat de l'évaluation du nœud.
     * @throws Exception si une erreur se produit pendant l'évaluation.
     */
    T eval(ExecutionStateI executionState) throws Exception;
}
