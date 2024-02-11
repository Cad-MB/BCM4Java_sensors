package connectors;

import ast.query.Query;
import components.interfaces.ClientCI;
import components.interfaces.NetworkNodeCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;

import java.util.ArrayList;

public class Connector
    extends AbstractConnector
    implements ClientCI
{
    @Override
    public ArrayList<String> sendRequest(Query q) throws Exception {
        return ((NetworkNodeCI)this.offering).evaluation(q);
    }
}
