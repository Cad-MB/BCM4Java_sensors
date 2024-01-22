package ast.query;

import ast.bexp.BExp;
import ast.cont.Cont;

public class BQuery extends Query {
    BExp bExp;
    Cont cont;

    public BQuery(BExp bExp, Cont cont) {
        this.bExp = bExp;
        this.cont = cont;
    }
}
