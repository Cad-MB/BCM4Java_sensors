package configs;

import visualization.Visualisation;

public class bigTest50 {

    public static void main(String[] args) throws Exception {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        Visualisation.main(new String[]{ "client-stress-test-Big50" });
    }

}
