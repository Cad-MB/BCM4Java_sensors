package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

public class NodeInfo
    implements NodeInfoI {

    double range;
    String id;
    PositionI position;
    private EndPointDescriptorI endPointInfo;
    private EndPointDescriptorI p2pEndPointInfo;

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

    public static class EndPointInfo
        implements EndPointDescriptorI {

        String uri;

        public EndPointInfo(String uri) {
            this.uri = uri;
        }

        @Override
        public String toString() {
            return uri;
        }

    }

}