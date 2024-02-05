package ast.dirs;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

import java.util.Set;

public abstract class Dirs implements QueryI, ASTNode<Set<Direction>> {
}
