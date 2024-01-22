package ast.cexp;

import ast.bexp.BExp;

public class LCExp extends CExp {
    BExp bExp1;
    BExp bExp2;

    public LCExp(BExp bExp1, BExp bExp2) {
        this.bExp1 = bExp1;
        this.bExp2 = bExp2;
    }
}
