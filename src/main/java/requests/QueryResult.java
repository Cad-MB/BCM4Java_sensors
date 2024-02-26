package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.ArrayList;

public class QueryResult
    implements QueryResultI {

    boolean isBoolean; // Indique si la requête est de type booléen ou de type collecte de données
    ArrayList<String> positiveSensorNodes; // Liste des nœuds de capteurs répondant positivement à la requête booléenne
    ArrayList<SensorDataI> gatheredSensorValues;
    // Liste des données de capteurs collectées en réponse à la requête de collecte

    // Constructeur pour une requête booléenne
    public QueryResult(
        boolean isBoolean, ArrayList<String> positiveSensorNodes, ArrayList<SensorDataI> gatheredSensorValues
    ) {
        this.isBoolean = isBoolean;
        this.positiveSensorNodes = positiveSensorNodes;
        this.gatheredSensorValues = gatheredSensorValues;
    }

    // Constructeur pour une requête booléenne sans nœuds de capteurs positifs ni données collectées
    public QueryResult(boolean isBoolean) {
        this.isBoolean = isBoolean;
        this.positiveSensorNodes = new ArrayList<>();
        this.gatheredSensorValues = new ArrayList<>();
    }

    // Méthode pour vérifier si la requête est de type booléen
    @Override
    public boolean isBooleanRequest() {
        return isBoolean;
    }

    // Méthode pour obtenir la liste des nœuds de capteurs répondant positivement à la requête booléenne
    @Override
    public ArrayList<String> positiveSensorNodes() {
        return positiveSensorNodes;
    }

    // Méthode pour vérifier si la requête est de type collecte de données
    @Override
    public boolean isGatherRequest() {
        return !isBoolean;
    }

    // Méthode pour obtenir la liste des données de capteurs collectées en réponse à la requête de collecte
    @Override
    public ArrayList<SensorDataI> gatheredSensorsValues() {
        return gatheredSensorValues;
    }

    // Méthode pour ajouter une donnée de capteur à la liste des données collectées
    public void addSensorValue(SensorDataI sensorValue) {
        gatheredSensorValues.add(sensorValue);
    }

    // Méthode pour ajouter un nœud de capteur à la liste des nœuds répondant positivement à la requête booléenne
    public void addPositiveNode(String nodeId) {
        positiveSensorNodes.add(nodeId);
    }

}
