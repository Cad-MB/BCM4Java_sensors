package ast.cexp;

import ast.ASTNode;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

/**
 * This abstract class represents a comparison expression in the abstract syntax tree (AST).
 * It extends the QueryI interface, used to represent queries in the sensor network,
 * as well as the ASTNode interface for manipulating nodes in the abstract syntax tree.
 * This class provides a common base for other specific comparison expression classes.
 */
public interface CExp
    extends QueryI, ASTNode<Boolean> {
    // Les détails de l'implémentation spécifique seront fournis dans les sous-classes.
}
