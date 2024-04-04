package sensor_network;

import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

/**
 * This class represents a geographical zone defined by a center position and a radius.
 * It implements the {@link fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI} interface.
 */
public class GeographicalZone
    implements GeographicalZoneI {

    protected PositionI centre;
    protected double rayon;

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
