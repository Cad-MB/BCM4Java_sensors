package ast.cexp;

import ast.bexp.BExp;

public class EqCExp extends CExp {
    BExp bExp1;
    BExp bExp2;

    public EqCExp(BExp bExp1, BExp bExp2) {
        this.bExp1 = bExp1;
        this.bExp2 = bExp2;
    }
}
