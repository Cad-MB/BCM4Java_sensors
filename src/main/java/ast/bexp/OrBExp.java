package ast.bexp;

public class OrBExp extends BExp {
    BExp bExp1;
    BExp bExp2;

    public OrBExp(BExp bExp1, BExp bExp2) {
        this.bExp1 = bExp1;
        this.bExp2 = bExp2;
    }
}
