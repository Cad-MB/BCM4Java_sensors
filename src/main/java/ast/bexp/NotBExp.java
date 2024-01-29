package ast.bexp;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class NotBExp extends BExp {
    BExp bExp;

    public NotBExp(BExp bExp) {
        this.bExp = bExp;
    }

    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        return !bExp.eval(executionState);
    }
}
