package ast.query;

import ast.cont.Cont;
import ast.gather.Gather;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

import java.util.ArrayList;

public class GQuery extends Query<Object> {
    Gather<String, Object> gather;
    Cont cont;

    public GQuery(Gather<String, Object> gather, Cont cont) {
        this.gather = gather;
        this.cont = cont;
    }

    @Override
    public ArrayList<Object> eval(ExecutionStateI executionState) throws Exception {
        ArrayList<Object> results = new ArrayList<>();
        results.add(gather.eval(executionState));
        return results;
    }
}
