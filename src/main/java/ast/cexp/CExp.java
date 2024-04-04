package ast.cexp;

import ast.ASTNode;

import java.io.Serializable;

/**
 * This abstract class represents a comparison expression in the abstract syntax tree (AST).
 * It extends the QueryI interface, used to represent queries in the sensor network,
 * as well as the ASTNode interface for manipulating nodes in the abstract syntax tree.
 * This class provides a common base for other specific comparison expression classes.
 */
public interface CExp
    extends ASTNode<Boolean> {

    static double getDoubleOfNumber(Serializable s) {
        if (!(s instanceof Number)) throw new RuntimeException("Type does not match");
        return ((Number) s).doubleValue();
    }

}
