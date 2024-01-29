import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.ArrayList;

public class QueryResult implements QueryResultI {
    boolean isBoolean;
    ArrayList<String> positiveSensorNodes;
    ArrayList<SensorDataI> gatheredSensorValues;

    public QueryResult(boolean isBoolean, ArrayList<String> positiveSensorNodes, ArrayList<SensorDataI> gatheredSensorValues) {
        this.isBoolean = isBoolean;
        this.positiveSensorNodes = positiveSensorNodes;
        this.gatheredSensorValues = gatheredSensorValues;
    }

    @Override
    public boolean isBooleanRequest() {
        return isBoolean;
    }

    @Override
    public ArrayList<String> positiveSensorNodes() {
        return positiveSensorNodes;
    }

    @Override
    public boolean isGatherRequest() {
        return !isBoolean;
    }

    @Override
    public ArrayList<SensorDataI> gatheredSensorsValues() {
        return gatheredSensorValues;
    }
}
