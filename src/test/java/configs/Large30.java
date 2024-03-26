package configs;

import cvm.CVM;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.util.HashSet;

public class Large30 {

    public static void main(String[] args) throws Exception {
        HashSet<SensorDataI> sensorsAll = new HashSet<>();
        CVM small3 = new CVM("large30", sensorsAll);
        small3.startStandardLifeCycle(200000L);
        new CVM.SensorRandomizer(sensorsAll).start();
        // Visualisation.main(new String[]{ "large30" });
    }

}
