import components.Client;
import components.Node;
import connectors.Connector;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;

public class CVM
    extends AbstractCVM
{
    public CVM() throws Exception {}

    @Override
    public void deploy() throws Exception
    {
        AbstractComponent.createComponent(Node.class.getCanonicalName(), new Object[]{});
        String cURI = AbstractComponent.createComponent(Client.class.getCanonicalName(), new Object[]{});
        this.doPortConnection(cURI, Client.COP_URI, Node.NNIP_URI, Connector.class.getCanonicalName());
        super.deploy();
    }
    public static void main(String[] args) throws Exception {
        CVM c = new CVM();
        c.startStandardLifeCycle(10000L);
        System.exit(0);
    }
}
