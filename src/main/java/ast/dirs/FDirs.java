package ast.dirs;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.Set;

/**
 * Cette classe représente une liste de directions avec une seule direction dans l'arbre de syntaxe abstraite (AST) des requêtes.
 * Elle étend la classe Dirs et implémente la méthode eval pour ajouter une seule direction à l'ensemble de directions actuel.
 */
public class FDirs extends Dirs {
    /** La direction à ajouter à l'ensemble de directions. */
    Direction dir;

    /**
     * Constructeur de la classe FDirs.
     * @param dir La direction à ajouter à l'ensemble de directions.
     */
    public FDirs(Direction dir) {
        this.dir = dir;
    }

    /**
     * Ajoute la direction spécifiée à l'ensemble de directions actuel.
     * @param executionState L'état d'exécution actuel.
     * @return L'ensemble de directions mis à jour avec la direction spécifiée.
     */
    @Override
    public Set<Direction> eval(ExecutionStateI executionState) {
        Set<Direction> directions = executionState.getDirections();
        directions.add(dir);
        return directions;
    }
}
