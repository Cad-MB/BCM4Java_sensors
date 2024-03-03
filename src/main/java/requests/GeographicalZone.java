package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

public class GeographicalZone
    implements GeographicalZoneI {

    PositionI centre;
    double rayon;

    /**
     * Constructs a {@code GeographicalZone} object with the given center position and radius.
     *
     * @param centre the center position of the geographical zone
     * @param rayon  the radius of the geographical zone
     */
    public GeographicalZone(PositionI centre, double rayon) {
        this.centre = centre;
        this.rayon = rayon;
    }

    @Override
    public boolean in(PositionI p) {
        return p.distance(centre) < rayon;
    }

}
