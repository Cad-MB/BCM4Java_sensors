package sensor_network;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;

public class BCM4JavaEndPointDescriptor
    implements BCM4JavaEndPointDescriptorI {

    protected final String inboundPortURI;
    protected Class<? extends OfferedCI> offeredInterface;

    /**
     * Constructs a BCM4JavaEndPointDescriptor with the specified inbound port URI and offered interface.
     *
     * @param inboundPortURI The URI of the inbound port.
     * @param offeredInterface The class of the interface offered by this endpoint.
     */
    public BCM4JavaEndPointDescriptor(String inboundPortURI, Class<? extends OfferedCI> offeredInterface) {
        this.inboundPortURI = inboundPortURI;
        this.offeredInterface = offeredInterface;
    }

    /**
     * Returns the URI of the inbound port associated with this endpoint.
     *
     * @return The inbound port URI.
     */
    @Override
    public String getInboundPortURI() {
        return inboundPortURI;
    }

    /**
     * Checks if the specified class is the offered interface or a superclass/interface of the offered interface.
     *
     * @param aClass The class to check against the offered interface.
     * @return True if the specified class is the offered interface or a superclass/interface, false otherwise.
     */
    @Override
    public boolean isOfferedInterface(Class<? extends OfferedCI> aClass) {
        return aClass.isInstance(offeredInterface);
    }

}
