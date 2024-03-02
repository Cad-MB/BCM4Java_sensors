package ast.bexp;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * Cette classe représente une expression booléenne "OU" dans l'arbre de syntaxe abstraite (AST).
 * Elle étend la classe BExp et implémente la méthode eval pour évaluer l'expression.
 */
public class OrBExp
    implements BExp {

    /**
     * La première expression booléenne à évaluer.
     */
    BExp bExp1;
    /**
     * La deuxième expression booléenne à évaluer.
     */
    BExp bExp2;

    /**
     * Constructeur de la classe OrBExp.
     *
     * @param bExp1 La première expression booléenne à évaluer.
     * @param bExp2 La deuxième expression booléenne à évaluer.
     */
    public OrBExp(BExp bExp1, BExp bExp2) {
        this.bExp1 = bExp1;
        this.bExp2 = bExp2;
    }

    /**
     * Évalue l'expression booléenne "OU".
     *
     * @param executionState L'état d'exécution actuel.
     * @return Le résultat de l'évaluation de l'expression booléenne "OU".
     * @throws Exception Si une erreur se produit lors de l'évaluation des expressions booléennes.
     */
    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        // Évalue les deux expressions booléennes et retourne leur disjonction logique
        return bExp1.eval(executionState) || bExp2.eval(executionState);
    }

}
