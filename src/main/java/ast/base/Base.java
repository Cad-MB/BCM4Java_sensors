package ast.base;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

public abstract class Base implements QueryI, ASTNode<PositionI> {
}
