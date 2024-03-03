package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

import java.util.Objects;

/**
 * This class represents information about a sensor network node, including its range, ID, position,
 * and endpoint descriptors.
 * It implements the {@link fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI} interface.
 */
public class NodeInfo
    implements NodeInfoI {

    double range;
    String id;
    PositionI position;
    private EndPointDescriptorI endPointInfo;
    private EndPointDescriptorI p2pEndPointInfo;

    /**
     * Constructs a {@code NodeInfo} object with the given range, ID, and position.
     *
     * @param range    the range of the node
     * @param id       the ID of the node
     * @param position the position of the node
     */
    public NodeInfo(double range, String id, PositionI position) {
        this.range = range;
        this.id = id;
        this.position = position;
    }

    @Override
    public EndPointDescriptorI p2pEndPointInfo() {
        return p2pEndPointInfo;
    }

    @Override
    public PositionI nodePosition() {
        return position;
    }

    @Override
    public double nodeRange() {
        return range;
    }

    public void setP2pEndPointInfo(EndPointDescriptorI p2pEndPointInfo) {
        this.p2pEndPointInfo = p2pEndPointInfo;
    }

    @Override
    public EndPointDescriptorI endPointInfo() {
        return this.endPointInfo;
    }

    @Override
    public String nodeIdentifier() {
        return id;
    }

    public void setEndPointInfo(EndPointDescriptorI endPointInfo) {
        this.endPointInfo = endPointInfo;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
               "id=" + id +
               ", position=" + position +
               '}';
    }

    /**
     * The {@code EndPointInfo} class represents an endpoint descriptor.
     * It implements the {@link fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI} interface.
     */
    public static class EndPointInfo
        implements EndPointDescriptorI {

        String uri;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final NodeInfo nodeInfo = (NodeInfo) o;
        return Objects.equals(id, nodeInfo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
