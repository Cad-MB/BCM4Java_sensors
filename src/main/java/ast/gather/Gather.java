package ast.gather;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

import java.util.HashMap;

public interface Gather<K, SensorDataI>
    extends QueryI, ASTNode<HashMap<K, SensorDataI>> {
}
