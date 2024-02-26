package ast.bexp;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * Cette classe représente une expression booléenne "NON" dans l'arbre de syntaxe abstraite (AST).
 * Elle étend la classe BExp et implémente la méthode eval pour évaluer l'expression.
 */
public class NotBExp
    extends BExp {

    /**
     * L'expression booléenne à nier.
     */
    BExp bExp;

    /**
     * Constructeur de la classe NotBExp.
     *
     * @param bExp L'expression booléenne à nier.
     */
    public NotBExp(BExp bExp) {
        this.bExp = bExp;
    }

    /**
     * Évalue l'expression booléenne "NON".
     *
     * @param executionState L'état d'exécution actuel.
     * @return Le résultat de l'évaluation de l'expression booléenne négative.
     * @throws Exception Si une erreur se produit lors de l'évaluation de l'expression booléenne.
     */
    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        // Négation du résultat de l'évaluation de l'expression booléenne
        return !bExp.eval(executionState);
    }

}