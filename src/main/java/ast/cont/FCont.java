package ast.cont;

import ast.base.Base;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import requests.ExecutionState;

/**
 * Cette classe représente une continuation d'inondation dans l'arbre de syntaxe abstraite (AST) des requêtes.
 * Elle étend la classe Cont et implémente la méthode eval pour configurer l'état d'exécution avec la portée maximale spécifiée.
 */
public class FCont
    implements Cont {

    Base base;
    /**
     * La distance maximale de la base spécifiée dans la continuation d'inondation.
     */
    double distance;

    /**
     * Constructeur de la classe FCont.
     *
     * @param distance La distance maximale de la base spécifiée dans la continuation d'inondation.
     */
    public FCont(Base base, double distance) {
        this.base = base;
        this.distance = distance;
    }

    /**
     * Configure l'état d'exécution avec la portée maximale spécifiée dans la continuation d'inondation.
     *
     * @param executionState L'état d'exécution actuel.
     * @return null car cette méthode ne retourne pas de résultat spécifique.
     * @throws Exception Si une erreur se produit lors de la configuration de l'état d'exécution.
     */
    @Override
    public Void eval(ExecutionStateI executionState) throws Exception {
        base.eval(executionState);
        assert executionState instanceof ExecutionState;
        // Configure l'état d'exécution avec la portée maximale spécifiée et active le mode d'inondation
        ((ExecutionState) executionState).setFlooding(true);
        ((ExecutionState) executionState).setMaxDistance(distance);
        return null;
    }

}