package configs;

import visualization.Visualisation;

public class Client_stress_test_180 {
    public static void main(String[] args) throws Exception {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        Visualisation.main(new String[]{ "client-stress-test-180" });
    }
}
