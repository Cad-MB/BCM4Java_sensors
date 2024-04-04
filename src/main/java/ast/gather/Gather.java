package ast.gather;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.List;

/**
 * This interface represents a gather operation in the abstract syntax tree (AST) of queries.
 * It extends the QueryI interface and the ASTNode class to represent a gather operation that produces a mapping of keys to sensor data.
 */
public interface Gather
    extends ASTNode<List<SensorDataI>> {
}
