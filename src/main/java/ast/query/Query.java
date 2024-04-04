package ast.query;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

/**
 * This abstract class represents a query in the abstract syntax tree (AST).
 * It implements the QueryI interface, used to represent queries in the sensor network,
 * as well as the ASTNode interface for manipulation of abstract syntax tree nodes.
 * This class provides a common base for other specific query classes.
 */
public interface Query
    extends QueryI, ASTNode<QueryResultI> {
}
