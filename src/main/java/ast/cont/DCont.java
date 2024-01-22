package ast.cont;

import ast.dirs.Dirs;

public class DCont extends Cont {
    Dirs dirs;
    int nbSauts;

    public DCont(Dirs dirs, int nbSauts) {
        this.dirs = dirs;
        this.nbSauts = nbSauts;
    }
}
