package components.node;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;

public interface NodeP2PInCI
    extends OfferedCI {

    void connect(NodeInfoI neighbour) throws Exception;
    void disconnect(NodeInfoI neighbour) throws Exception;

}
