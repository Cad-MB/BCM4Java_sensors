package ast.query;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

import java.util.ArrayList;

public abstract class Query<T> implements QueryI, ASTNode<ArrayList<T>> {
}
