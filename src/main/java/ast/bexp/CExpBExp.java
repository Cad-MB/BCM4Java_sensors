package ast.bexp;

import ast.cexp.CExp;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * Cette classe représente une expression booléenne basée sur une expression de comparaison dans l'arbre de syntaxe abstraite (AST).
 * Elle étend la classe BExp et implémente la méthode eval pour évaluer l'expression.
 */
public class CExpBExp
    implements BExp {

    /**
     * L'expression de comparaison à évaluer.
     */
    CExp cExp;

    /**
     * Constructeur de la classe CExpBExp.
     *
     * @param cExp L'expression de comparaison à évaluer.
     */
    public CExpBExp(CExp cExp) {
        this.cExp = cExp;
    }

    /**
     * Évalue l'expression de comparaison pour obtenir le résultat de l'expression booléenne.
     *
     * @param executionState L'état d'exécution actuel.
     * @return Le résultat de l'évaluation de l'expression de comparaison, représentant le résultat de l'expression booléenne.
     * @throws Exception Si une erreur se produit lors de l'évaluation de l'expression de comparaison.
     */
    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        return cExp.eval(executionState); // Évalue l'expression de comparaison et retourne son résultat
    }

}
