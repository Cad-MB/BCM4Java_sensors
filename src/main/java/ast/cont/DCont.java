package ast.cont;

import ast.dirs.Dirs;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import requests.ExecutionState;

/**
 * Cette classe représente une continuation directionnelle dans l'arbre de syntaxe abstraite (AST) des requêtes.
 * Elle étend la classe Cont et implémente la méthode eval pour configurer l'état d'exécution avec les directions spécifiées.
 */
public class DCont extends Cont {
    /** Les directions à suivre dans la continuation directionnelle. */
    Dirs dirs;
    /** Le nombre maximal de sauts autorisés dans la continuation directionnelle. */
    int nbSauts;

    /**
     * Constructeur de la classe DCont.
     * @param dirs Les directions à suivre dans la continuation directionnelle.
     * @param nbSauts Le nombre maximal de sauts autorisés dans la continuation directionnelle.
     */
    public DCont(Dirs dirs, int nbSauts) {
        this.dirs = dirs;
        this.nbSauts = nbSauts;
    }

    /**
     * Configure l'état d'exécution avec les directions spécifiées dans la continuation directionnelle.
     * @param executionState L'état d'exécution actuel.
     * @return null car cette méthode ne retourne pas de résultat spécifique.
     * @throws Exception Si une erreur se produit lors de la configuration de l'état d'exécution.
     */
    @Override
    public Void eval(ExecutionStateI executionState) throws Exception {
        assert executionState instanceof ExecutionState;
        // Configure l'état d'exécution avec les directions spécifiées et active le mode directionnel
        ((ExecutionState) executionState).setDirectional(true);
        ((ExecutionState) executionState).setDirections(dirs.eval(executionState));
        return null;
    }
}
