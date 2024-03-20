package configs;

import visualization.Visualisation;

public class Small3 {

    public static void main(String[] args) throws Exception {
        // HashSet<SensorDataI> sensorsAll = new HashSet<>();
        // CVM small3 = new CVM("small3", sensorsAll);
        // small3.startStandardLifeCycle(200000L);
        // new CVM.SensorRandomizer(sensorsAll).start();
        Visualisation.main(new String[]{ "small3" });
    }

}
