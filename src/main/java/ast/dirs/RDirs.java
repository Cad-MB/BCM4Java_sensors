package ast.dirs;

public class RDirs extends Dirs {
    Dir dir;
    Dirs dirs;

    public RDirs(Dir dir, Dirs dirs) {
        this.dir = dir;
        this.dirs = dirs;
    }
}
