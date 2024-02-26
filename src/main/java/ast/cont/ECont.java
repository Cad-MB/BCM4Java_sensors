package ast.cont;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import requests.ExecutionState;

/**
 * Cette classe représente une continuation vide dans l'arbre de syntaxe abstraite (AST) des requêtes.
 * Elle étend la classe Cont et implémente la méthode eval pour configurer l'état d'exécution comme directionnel sans aucune direction spécifiée.
 */
public class ECont
    extends Cont {

    /**
     * Configure l'état d'exécution comme directionnel sans aucune direction spécifiée.
     *
     * @param executionState L'état d'exécution actuel.
     * @return null car cette méthode ne retourne pas de résultat spécifique.
     * @throws Exception Si une erreur se produit lors de la configuration de l'état d'exécution.
     */
    @Override
    public Void eval(ExecutionStateI executionState) throws Exception {
        assert executionState instanceof ExecutionState;
        // Configure l'état d'exécution comme directionnel sans aucune direction spécifiée
        ((ExecutionState) executionState).setDirectional(true);
        ((ExecutionState) executionState).setDirections(null);
        return null;
    }

}
