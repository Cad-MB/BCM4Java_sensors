package ast.bexp;

public class AndBExp extends BExp {
    BExp bExp1;
    BExp bExp2;

    public AndBExp(BExp bExp1, BExp bExp2) {
        this.bExp1 = bExp1;
        this.bExp2 = bExp2;
    }
}
