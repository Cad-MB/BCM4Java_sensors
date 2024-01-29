package ast.rand;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class CRand extends Rand {
    double constant;

    public CRand(double constant) {
        this.constant = constant;
    }

    @Override
    public Double eval(ExecutionStateI executionState) throws Exception {
        return constant;
    }
}
