package configs;

import visualization.Visualisation;

public class stress_test_2 {

    public static void main(String[] args) throws Exception {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        // TestThreadsByThreadCount.main(new String[]{});
        Visualisation.main(new String[]{ "client-stress-test-threads-5" });
    }
}
