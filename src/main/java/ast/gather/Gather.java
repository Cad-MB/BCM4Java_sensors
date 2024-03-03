package ast.gather;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

import java.util.HashMap;

/**
 * This interface represents a gather operation in the abstract syntax tree (AST) of queries.
 * It extends the QueryI interface and the ASTNode class to represent a gather operation that produces a mapping of keys to sensor data.
 *
 * @param <K>           the type of keys in the mapping
 * @param <SensorDataI> the type of sensor data
 */
public interface Gather<K, SensorDataI>
    extends QueryI, ASTNode<HashMap<K, SensorDataI>> {
}
