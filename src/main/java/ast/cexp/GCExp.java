package ast.cexp;

import ast.rand.Rand;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class GCExp extends CExp {
    Rand rand1;
    Rand rand2;

    public GCExp(Rand rand1, Rand rand2) {
        this.rand1 = rand1;
        this.rand2 = rand2;
    }

    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        Double r1 = rand1.eval(executionState);
        Double r2 = rand2.eval(executionState);
        return r1.compareTo(r2) > 0;
    }
}
