package components.interfaces;

import ast.query.Query;
import fr.sorbonne_u.components.interfaces.OfferedCI;

import java.util.ArrayList;

public interface NetworkNodeCI
extends OfferedCI
{
    ArrayList<String> evaluation (Query q) throws Exception;
}
