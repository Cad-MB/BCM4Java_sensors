package ast.bexp;

import ast.cexp.CExp;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class CExpBExp extends BExp {
    CExp cExp;

    public CExpBExp(CExp cExp) {
        this.cExp = cExp;
    }

    @Override
    public Boolean eval(ExecutionStateI executionState) throws Exception {
        return cExp.eval(executionState);
    }
}
