package ast.base;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * Cette classe représente une base avec une position dynamique déterminée par l'exécution dans l'arbre de syntaxe abstraite (AST) des requêtes.
 * Elle étend la classe Base et implémente la méthode eval pour retourner la position de la base en cours d'exécution.
 */
public class RBase
    implements Base {

    /**
     * Constructeur de la classe RBase.
     */
    public RBase() {
    }

    /**
     * Retourne la position de la base en cours d'exécution.
     *
     * @param executionState L'état d'exécution actuel.
     * @return La position de la base en cours d'exécution.
     */
    @Override
    public PositionI eval(ExecutionStateI executionState) {
        return executionState.getProcessingNode().getPosition();
    }

}
