package ast.cexp;

import ast.rand.Rand;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * Cette classe représente une expression de comparaison "plus petit" dans l'arbre de syntaxe abstraite (AST).
 * Elle étend la classe CExp et implémente la méthode eval pour évaluer l'expression.
 */
public class LCExp
    implements CExp {

    /**
     * Le premier opérande de l'expression de comparaison.
     */
    Rand rand1;
    /**
     * Le deuxième opérande de l'expression de comparaison.
     */
    Rand rand2;

    /**
     * Constructeur de la classe LCExp.
     *
     * @param rand1 Le premier opérande de l'expression de comparaison.
     * @param rand2 Le deuxième opérande de l'expression de comparaison.
     */
    public LCExp(Rand rand1, Rand rand2) {
        this.rand1 = rand1;
        this.rand2 = rand2;
    }

    /**
     * Évalue l'expression de comparaison "plus petit".
     *
     * @param executionState L'état d'exécution actuel.
     * @return Le résultat de l'évaluation de l'expression de comparaison "plus petit".
     * @throws Exception Si une erreur se produit lors de l'évaluation des opérandes de l'expression de comparaison.
     */
    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        // Évalue les deux opérandes de l'expression de comparaison et retourne le résultat de la comparaison
        Double r1 = rand1.eval(executionState);
        Double r2 = rand2.eval(executionState);
        return r1.compareTo(r2) <
               0; // Compare les deux valeurs et retourne vrai si le premier est plus petit que le deuxième
    }

}
