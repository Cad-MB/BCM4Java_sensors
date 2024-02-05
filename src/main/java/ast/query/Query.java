package ast.query;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

public abstract class Query implements QueryI, ASTNode<QueryResultI> {
}
