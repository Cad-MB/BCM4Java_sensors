package connectors;

import ast.query.Query;
import components.interfaces.ClientCI;
import components.interfaces.NetworkNodeCI;
import fr.sorbonne_u.components.connectors.AbstractConnector;

public class Connector
    extends AbstractConnector
    implements ClientCI
{
    @Override
    public String sendRequest(Query q) throws Exception {
        return ((NetworkNodeCI)this.offering).evaluation(q);
    }
}
