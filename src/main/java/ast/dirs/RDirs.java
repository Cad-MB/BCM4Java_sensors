package ast.dirs;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.Set;

/**
 * Cette classe représente une liste de directions avec une direction supplémentaire suivie d'une autre liste de directions dans l'arbre de syntaxe abstraite (AST) des requêtes.
 * Elle étend la classe Dirs et implémente la méthode eval pour ajouter la direction spécifiée ainsi que les directions évaluées de la liste suivante à l'ensemble de directions actuel.
 */
public class RDirs
    implements Dirs {

    /**
     * La direction à ajouter à l'ensemble de directions.
     */
    Direction dir;
    /**
     * La liste suivante de directions à évaluer.
     */
    Dirs dirs;

    /**
     * Constructeur de la classe RDirs.
     *
     * @param dir  La direction à ajouter à l'ensemble de directions.
     * @param dirs La liste suivante de directions à évaluer.
     */
    public RDirs(Direction dir, Dirs dirs) {
        this.dir = dir;
        this.dirs = dirs;
    }

    /**
     * Ajoute la direction spécifiée ainsi que les directions évaluées de la liste suivante à l'ensemble de directions actuel.
     *
     * @param executionState L'état d'exécution actuel.
     * @return L'ensemble de directions mis à jour avec la direction spécifiée et les directions évaluées de la liste suivante.
     * @throws Exception Si une erreur se produit lors de l'évaluation des directions de la liste suivante.
     */
    @Override
    public Set<Direction> eval(ExecutionStateI executionState) throws Exception {
        Set<Direction> directions = executionState.getDirections();
        // Ajoute la direction spécifiée à l'ensemble de directions
        directions.add(dir);
        // Évalue les directions de la liste suivante et les ajoute à l'ensemble de directions actuel
        Set<Direction> evaluatedDirections = dirs.eval(executionState);
        directions.addAll(evaluatedDirections);
        return directions;
    }

}
