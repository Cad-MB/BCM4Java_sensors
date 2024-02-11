package ast.query;

import ast.cont.Cont;
import ast.gather.Gather;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import queryResult.QueryResult;

/**
 * Cette classe représente une requête de collecte de données capteur dans l'arbre de syntaxe abstraite (AST).
 * Elle étend la classe abstraite Query et implémente la méthode eval pour évaluer la requête.
 */
public class GQuery extends Query {
    /** L'opération de collecte à effectuer. */
    Gather<String, QueryResultI> gather;
    /** L'identifiant du capteur dont on veut obtenir les valeurs. */
    String sensorId;
    /** La continuation de la requête. */
    Cont cont;

    /**
     * Constructeur de la classe GQuery.
     * @param gather L'opération de collecte à effectuer.
     * @param sensorId L'identifiant du capteur dont on veut obtenir les valeurs.
     * @param cont La continuation de la requête.
     */
    public GQuery(Gather<String, QueryResultI> gather, String sensorId, Cont cont) {
        this.gather = gather;
        this.sensorId = sensorId;
        this.cont = cont;
    }

    /**
     * Évalue la requête de collecte de données capteur.
     * @param executionState L'état d'exécution actuel.
     * @return Le résultat de la requête de collecte de données capteur.
     * @throws Exception Si une erreur se produit lors de l'évaluation de la continuation.
     */
    @Override
    public QueryResultI eval(ExecutionStateI executionState) throws Exception {
        cont.eval(executionState); // Évaluation de la continuation
        QueryResult result = new QueryResult(false); // Création d'un objet QueryResult initialisé à faux
        // Récupération des valeurs des capteurs et ajout au résultat
        gather.eval(executionState).forEach((k, v) -> result.addSensorValue(v));
        return result; // Retour du résultat de la requête
    }
}
