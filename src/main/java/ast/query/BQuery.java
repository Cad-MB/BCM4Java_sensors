package ast.query;

import ast.bexp.BExp;
import ast.cont.Cont;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import requests.QueryResult;

/**
 * Cette classe représente une requête booléenne dans l'arbre de syntaxe abstraite (AST).
 * Elle étend la classe abstraite Query et implémente la méthode eval pour évaluer la requête.
 */
public class BQuery extends Query {
    /** L'expression booléenne à évaluer. */
    BExp bExp;
    /** La continuation de la requête. */
    Cont cont;

    /**
     * Constructeur de la classe BQuery.
     * @param bExp L'expression booléenne à évaluer.
     * @param cont La continuation de la requête.
     */
    public BQuery(BExp bExp, Cont cont) {
        this.bExp = bExp;
        this.cont = cont;
    }

    /**
     * Évalue la requête booléenne.
     * @param executionState L'état d'exécution actuel.
     * @return Le résultat de la requête booléenne.
     * @throws Exception Si une erreur se produit lors de l'évaluation de la continuation.
     */
    @Override
    public QueryResultI eval(ExecutionStateI executionState) throws Exception {
        cont.eval(executionState); // Évaluation de la continuation
        QueryResult result = new QueryResult(true); // Création d'un objet QueryResult initialisé à vrai
        // Si l'expression booléenne est vraie, ajouter l'identifiant du nœud positif au résultat
        if (bExp.eval(executionState))
            result.addPositiveNode(executionState.getProcessingNode().getNodeIdentifier());
        return result; // Retour du résultat de la requête
    }
}
