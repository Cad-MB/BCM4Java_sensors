package requests;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;

public class BCM4JavaEndPointDescriptor
    implements BCM4JavaEndPointDescriptorI {

    protected final String inboundPortURI;
    protected Class<? extends OfferedCI> offeredInterface;

    public BCM4JavaEndPointDescriptor(String inboundPortURI, Class<? extends OfferedCI> offeredInterface) {
        this.inboundPortURI = inboundPortURI;
        this.offeredInterface = offeredInterface;
    }

    @Override
    public String getInboundPortURI() {
        return inboundPortURI;
    }

    @Override
    public boolean isOfferedInterface(Class<? extends OfferedCI> aClass) {
        return aClass.isInstance(offeredInterface);
    }


    @Override
    public String toString() {
        return inboundPortURI;
    }

}
