package ast.query;

import ast.gather.Gather;
import ast.cont.Cont;

public class GQuery extends Query {
    Gather gather;
    Cont cont;

    public GQuery(Gather gather, Cont cont) {
        this.gather = gather;
        this.cont = cont;
    }
}
