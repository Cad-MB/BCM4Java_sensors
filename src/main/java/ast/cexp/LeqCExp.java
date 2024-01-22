package ast.cexp;

import ast.bexp.BExp;

public class LeqCExp extends CExp {
    BExp bExp1;
    BExp bExp2;

    public LeqCExp(BExp bExp1, BExp bExp2) {
        this.bExp1 = bExp1;
        this.bExp2 = bExp2;
    }
}
