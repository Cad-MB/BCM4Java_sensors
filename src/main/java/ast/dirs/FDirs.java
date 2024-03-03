package ast.dirs;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.Set;

/**
 * Cette classe représente une liste de directions avec une seule direction dans l'arbre de syntaxe abstraite (AST) des requêtes.
 * Elle étend la classe Dirs et implémente la méthode eval pour ajouter une seule direction à l'ensemble de directions actuel.
 */
public class FDirs
    implements Dirs {


    Direction dir;


    public FDirs(Direction dir) {
        this.dir = dir;
    }


    @Override
    public Set<Direction> eval(ExecutionStateI executionState) {
        Set<Direction> directions = executionState.getDirections();
        directions.add(dir);
        return directions;
    }

}
