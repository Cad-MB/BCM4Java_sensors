package ast.bexp;

public class NotBExp extends BExp {
    BExp bExp1;
    BExp bExp2;

    public NotBExp(BExp bExp1, BExp bExp2) {
        this.bExp1 = bExp1;
        this.bExp2 = bExp2;
    }
}
