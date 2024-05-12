package configs;

import visualization.Visualisation;

public class stress_test_2 {

    public static void main(String[] args) throws Exception {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        Visualisation.main(new String[]{ "client-stress-test-threads-1" });
    }

}
