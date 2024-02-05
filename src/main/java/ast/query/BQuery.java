package ast.query;

import ast.bexp.BExp;
import ast.cont.Cont;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.ArrayList;

public class BQuery extends Query<String> {
    BExp bExp;
    Cont cont;

    public BQuery(BExp bExp, Cont cont) {
        this.bExp = bExp;
        this.cont = cont;
    }

    @Override
    public ArrayList<String> eval(ExecutionStateI executionState) throws Exception {
        ArrayList<String> ids = new ArrayList<>();
        if (bExp.eval(executionState)) {
            ids.add(executionState.getProcessingNode().getNodeIdentifier());
        }
        return ids;
    }
}
