package ast.gather;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.HashMap;

/**
 * Cette classe représente une opération de collecte récursive dans l'arbre de syntaxe abstraite (AST).
 * Elle étend la classe Gather et implémente la méthode eval pour évaluer l'opération de collecte.
 */
public class RGather
    extends Gather<String, SensorDataI> {

    /**
     * L'identifiant du capteur à collecter.
     */
    String sensorId;
    /**
     * L'opération de collecte à appliquer récursivement.
     */
    Gather<String, SensorDataI> gather;

    /**
     * Constructeur de la classe RGather.
     *
     * @param sensorId L'identifiant du capteur à collecter.
     * @param gather   L'opération de collecte à appliquer récursivement.
     */
    public RGather(String sensorId, Gather<String, SensorDataI> gather) {
        this.sensorId = sensorId;
        this.gather = gather;
    }

    /**
     * Évalue l'opération de collecte récursive.
     *
     * @param executionState L'état d'exécution actuel.
     * @return Une map contenant les données collectées par cette opération et celles collectées par l'opération récursive.
     * @throws Exception Si une erreur se produit lors de l'évaluation de l'opération récursive.
     */
    @Override
    public HashMap<String, SensorDataI> eval(ExecutionStateI executionState) throws Exception {
        HashMap<String, SensorDataI> values = new HashMap<>();
        // Collecte les données du capteur spécifié
        values.put(sensorId, executionState.getProcessingNode().getSensorData(sensorId));
        // Évalue l'opération de collecte récursive et ajoute ses résultats à la map
        HashMap<String, SensorDataI> g = gather.eval(executionState);
        values.putAll(g);
        return values;
    }

}
