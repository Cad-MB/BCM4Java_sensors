package components.interfaces;

import ast.query.Query;
import fr.sorbonne_u.components.interfaces.RequiredCI;

public interface ClientCI
extends RequiredCI
{
    String sendRequest(Query q) throws Exception;
}
