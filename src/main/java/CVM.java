import components.Client;
import components.NetworkNode;
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
        AbstractComponent.createComponent(NetworkNode.class.getCanonicalName(), new Object[]{});
        String cURI = AbstractComponent.createComponent(Client.class.getCanonicalName(), new Object[]{});
        this.doPortConnection(cURI, Client.COP_URI, NetworkNode.NNIP_URI, Connector.class.getCanonicalName());
        super.deploy();
    }
    public static void main(String[] args) throws Exception {
        CVM c = new CVM();
        c.startStandardLifeCycle(10000L);
        System.exit(0);
    }
}
