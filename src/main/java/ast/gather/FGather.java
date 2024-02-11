package ast.gather;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.HashMap;

/**
 * Cette classe représente une opération de collecte ponctuelle dans l'arbre de syntaxe abstraite (AST).
 * Elle étend la classe Gather et implémente la méthode eval pour évaluer l'opération de collecte.
 */
public class FGather extends Gather<String, SensorDataI> {
    /** L'identifiant du capteur à collecter. */
    String sensorId;

    /**
     * Constructeur de la classe FGather.
     * @param sensorId L'identifiant du capteur à collecter.
     */
    public FGather(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * Évalue l'opération de collecte ponctuelle.
     * @param executionState L'état d'exécution actuel.
     * @return Une map contenant les données collectées par cette opération.
     */
    @Override
    public HashMap<String, SensorDataI> eval(ExecutionStateI executionState) {
        HashMap<String, SensorDataI> result = new HashMap<>();
        // Collecte les données du capteur spécifié et les ajoute à la map résultante
        result.put(sensorId, executionState.getProcessingNode().getSensorData(sensorId));
        return result; // Retourne la map contenant les données collectées
    }
}
