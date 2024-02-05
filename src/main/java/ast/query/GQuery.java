package ast.query;

import ast.cont.Cont;
import ast.gather.Gather;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import queryResult.QueryResult;

public class GQuery extends Query {
    Gather<String, QueryResultI> gather;
    String sensorId;
    Cont cont;

    public GQuery(Gather<String, QueryResultI> gather, String sensorId, Cont cont) {
        this.gather = gather;
        this.sensorId = sensorId;
        this.cont = cont;
    }

    @Override
    public QueryResultI eval(ExecutionStateI executionState) throws Exception {
        cont.eval(executionState);
        QueryResult result = new QueryResult(false);
        gather.eval(executionState).forEach((k, v) -> result.addSensorValue(v));
        return result;
    }
}
