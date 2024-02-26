package ast.rand;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * Cette classe représente un opérande aléatoire associé à un capteur dans l'arbre de syntaxe abstraite (AST).
 * Elle étend la classe Rand et implémente la méthode eval pour retourner la valeur du capteur.
 */
public class SRand
    extends Rand {

    /**
     * L'identifiant du capteur associé à l'opérande aléatoire.
     */
    String sensorId;

    /**
     * Constructeur de la classe SRand.
     *
     * @param sensorId L'identifiant du capteur associé à l'opérande aléatoire.
     */
    public SRand(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * Évalue l'opérande aléatoire associé à un capteur en récupérant sa valeur actuelle.
     *
     * @param executionState L'état d'exécution actuel.
     * @return La valeur actuelle du capteur associé à l'opérande aléatoire.
     * @throws Exception Si une erreur se produit lors de la récupération de la valeur du capteur.
     */
    @Override
    public Double eval(ExecutionStateI executionState) throws Exception {
        // todo
        // Récupère la valeur actuelle du capteur associé à l'opérande aléatoire dans l'état d'exécution
        // et la retourne (en supposant que la valeur du capteur est de type Double)
        return (Double) executionState.getProcessingNode().getSensorData(sensorId).getValue();
    }

}
