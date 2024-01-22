package ast.cexp;

import ast.bexp.BExp;

public class GCExp extends CExp {
    BExp bExp1;
    BExp bExp2;

    public GCExp(BExp bExp1, BExp bExp2) {
        this.bExp1 = bExp1;
        this.bExp2 = bExp2;
    }
}
