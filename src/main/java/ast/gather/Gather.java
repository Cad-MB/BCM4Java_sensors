package ast.gather;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

import java.util.HashMap;

public abstract class Gather<K, V> implements QueryI, ASTNode<HashMap<K, V>> {
}
