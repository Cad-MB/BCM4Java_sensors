package ast.rand;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * Cette classe représente un opérande aléatoire constant dans l'arbre de syntaxe abstraite (AST).
 * Elle étend la classe Rand et implémente la méthode eval pour retourner la valeur constante.
 */
public class CRand
    extends Rand {

    /**
     * La valeur constante de l'opérande aléatoire.
     */
    double constant;

    /**
     * Constructeur de la classe CRand.
     *
     * @param constant La valeur constante de l'opérande aléatoire.
     */
    public CRand(double constant) {
        this.constant = constant;
    }

    /**
     * Évalue l'opérande aléatoire constant.
     *
     * @param executionState L'état d'exécution actuel.
     * @return La valeur constante de l'opérande aléatoire.
     * @throws Exception Si une erreur se produit lors de l'évaluation de l'opérande.
     */
    @Override
    public Double eval(ExecutionStateI executionState) throws Exception {
        return constant; // Retourne la valeur constante de l'opérande
    }

}
