package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

public class GeographicalZone implements GeographicalZoneI {

    PositionI centre;
    double rayon;

    public GeographicalZone(PositionI centre, double rayon) {
        this.centre = centre;
        this.rayon = rayon;
    }

    @Override
    public boolean in(PositionI p) {
        return p.distance(centre) < rayon;
    }
}
