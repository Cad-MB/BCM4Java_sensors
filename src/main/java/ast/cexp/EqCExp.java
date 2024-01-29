package ast.cexp;

import ast.rand.Rand;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class EqCExp extends CExp {
    Rand rand1;
    Rand rand2;

    public EqCExp(Rand rand1, Rand rand2) {
        this.rand1 = rand1;
        this.rand2 = rand2;
    }

    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        return rand1.eval(executionState).equals(rand2.eval(executionState));
    }
}
