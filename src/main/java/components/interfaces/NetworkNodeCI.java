package components.interfaces;

import ast.query.Query;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface NetworkNodeCI
extends OfferedCI
{
    String evaluation (Query q) throws Exception;
}
