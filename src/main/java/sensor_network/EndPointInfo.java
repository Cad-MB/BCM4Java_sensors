package sensor_network;

import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;

/**
 * The {@code EndPointInfo} class represents an endpoint descriptor.
 * It implements the {@link fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI} interface.
 */
public class EndPointInfo
    implements EndPointDescriptorI {

    protected String uri;

    /**
     * Constructs an {@code EndPointInfo} object with the given URI.
     *
     * @param uri the URI of the endpoint
     */
    public EndPointInfo(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return uri;
    }

}
