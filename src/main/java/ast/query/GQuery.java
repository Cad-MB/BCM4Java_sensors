package ast.query;

import ast.cont.Cont;
import ast.gather.Gather;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import requests.QueryResult;

/**
 * This class represents a sensor data collection query in the abstract syntax tree (AST).
 * It extends the abstract Query class and implements the eval method to evaluate the query.
 */
public class GQuery
    implements Query {

    /**
     * The data collection operation to perform.
     */
    Gather<String, SensorDataI> gather;
    /**
     * The continuation of the query.
     */
    Cont cont;

    public GQuery(Gather<String, SensorDataI> gather, Cont cont) {
        this.gather = gather;
        this.cont = cont;
    }

    /**
     * Evaluates the sensor data collection query.
     *
     * @param executionState The current execution state.
     * @return The result of the sensor data collection query.
     * @throws Exception If an error occurs during the evaluation of the continuation.
     */
    @Override
    public QueryResultI eval(ExecutionStateI executionState) throws Exception {
        cont.eval(executionState); // Evaluation of the continuation
        QueryResult result = new QueryResult(false); // Creating a QueryResult object of gather type
        // Retrieving sensor values and adding them to the result
        gather.eval(executionState).forEach((k, v) -> result.addSensorValue(v));
        return result; // Return the query result
    }

}
