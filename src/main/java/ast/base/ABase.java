package ast.base;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * Cette classe représente une base avec une position spécifique dans l'arbre de syntaxe abstraite (AST) des requêtes.
 * Elle étend la classe Base et implémente la méthode eval pour retourner la position de la base.
 */
public class ABase
    extends Base {

    /**
     * La position de la base.
     */
    PositionI position;

    /**
     * Constructeur de la classe ABase.
     *
     * @param position La position de la base.
     */
    public ABase(PositionI position) {
        this.position = position;
    }

    /**
     * Retourne la position de la base.
     *
     * @param executionState L'état d'exécution actuel.
     * @return La position de la base.
     */
    @Override
    public PositionI eval(ExecutionStateI executionState) {
        return position;
    }

}
