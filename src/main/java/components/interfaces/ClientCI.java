package components.interfaces;

import ast.query.Query;
import fr.sorbonne_u.components.interfaces.RequiredCI;

import java.util.ArrayList;

public interface ClientCI
extends RequiredCI
{
    ArrayList<String> sendRequest(Query q) throws Exception;
}
