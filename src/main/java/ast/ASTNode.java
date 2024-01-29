package ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public interface ASTNode<T> {
    T eval(ExecutionStateI executionState) throws Exception;
}
