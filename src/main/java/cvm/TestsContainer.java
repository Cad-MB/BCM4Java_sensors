package cvm;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestsContainer {

    private static final String GREEN = "\033[0;32m";
    private static final String RED = "\033[0;31m";
    private static final String RESET = "\033[0m";

    public enum Status {
        OK, FAIL
    }

    private final HashMap<String, Status> resultsStatus = new HashMap<>();
    private final HashMap<String, Pair<List<Object>, List<Object>>> resultsMap = new HashMap<>();


    /**
     * Adds a test result as 'OK' (passed) for the specified test ID.
     *
     * @param id The identifier of the test that passed.
     */
    public synchronized void addOkResult(String id) {
        resultsStatus.put(id, Status.OK);
    }

    /**
     * Adds a test result as 'FAIL' (failed) for the specified test ID.
     * Also stores the expected and actual results for detailed reporting.
     *
     * @param id The identifier of the test that failed.
     * @param expectedResults The expected results for this test.
     * @param actualResults The actual results obtained from the test.
     */
    public synchronized void addFailResult(String id, List<Object> expectedResults, List<Object> actualResults) {
        resultsStatus.put(id, Status.FAIL);
        resultsMap.put(id, new Pair<>(expectedResults, actualResults));
    }

    /**
     * Prints a summary of all test results.
     * For each test, the method prints the test ID, its status (OK or FAIL),
     * and if failed, the expected and actual results.
     */
    public synchronized void recap() {
        if (resultsStatus.isEmpty()) {
            System.out.println("No results found");
        }
        for (Map.Entry<String, Status> entry : resultsStatus.entrySet()) {
            String id = entry.getKey();
            Status status = entry.getValue();
            if (status == Status.OK) {
                System.out.println(id + ": " + GREEN + "OK" + RESET);
            } else {
                List<Object> expectedResults = resultsMap.get(id).getKey();
                List<Object> actualResults = resultsMap.get(id).getValue();

                System.out.println(id + ": " + RED + "FAIL" + RESET);
                System.out.println("\t" + RED + "Expected: " + expectedResults + RESET);
                System.out.println("\t" + RED + "Actual: " + actualResults + RESET);
            }
        }
        System.out.println("\n");
    }

}
