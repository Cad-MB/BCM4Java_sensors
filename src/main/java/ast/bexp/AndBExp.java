package ast.bexp;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class AndBExp extends BExp {
    BExp bExp1;
    BExp bExp2;

    public AndBExp(BExp bExp1, BExp bExp2) {
        this.bExp1 = bExp1;
        this.bExp2 = bExp2;
    }

    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        return bExp1.eval(executionState) && bExp2.eval(executionState);
    }
}
