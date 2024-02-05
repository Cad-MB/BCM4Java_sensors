package ast.query;

import ast.bexp.BExp;
import ast.cont.Cont;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import queryResult.QueryResult;

public class BQuery extends Query {
    BExp bExp;
    Cont cont;

    public BQuery(BExp bExp, Cont cont) {
        this.bExp = bExp;
        this.cont = cont;
    }

    @Override
    public QueryResultI eval(ExecutionStateI executionState) throws Exception {
        cont.eval(executionState);
        QueryResult result = new QueryResult(true);
        if (bExp.eval(executionState))
            result.addPositiveNode(executionState.getProcessingNode().getNodeIdentifier());
        return result;
    }
}
