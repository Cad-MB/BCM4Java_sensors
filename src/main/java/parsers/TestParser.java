package parsers;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TestParser {

    /**
     * Parses the XML file into a Tests object.
     *
     * @param file The XML file containing the tests configurations.
     * @return A Tests object parsed from the file.
     * @throws Exception If there is an error during the parsing process.
     */
    public static Tests parse(File file) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(Tests.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        return ((Tests) unmarshaller.unmarshal(file));
    }

    @XmlRootElement(name="tests")
    public static class Tests
        implements Serializable {

        @XmlElement
        public Long executionDuration;

        @XmlElement(name="test")
        public ArrayList<Test> testList;

    }

    public static class Test
        implements Serializable {

        @XmlElement
        public String name;

        @XmlElement
        public String clientId;

        @XmlElement
        public String requestId;

        @XmlElement
        public int afterDelay;

        @XmlElement
        public boolean isBoolean;

        @XmlElement(name="nodeId")
        @XmlElementWrapper(name="expectBoolean")
        public List<String> nodeIds = new ArrayList<>();

        @XmlElement(name="sensor")
        @XmlElementWrapper(name="expectGather")
        public List<GatherResult> gatherResults = new ArrayList<>();

        public Test() { }

        /**
         * Constructs a Test with specified attributes.
         *
         * @param name The name of the test.
         * @param clientId The client ID associated with the test.
         * @param requestId The request ID associated with the test.
         * @param afterDelay The delay after which the test will be performed.
         * @param isBoolean Indicates if the test is a boolean test.
         * @param nodeIds The list of node IDs expected to be part of the result for boolean tests.
         * @param gatherResults The list of expected gather results for gather tests.
         */
        public Test(
            String name, String clientId, String requestId, int afterDelay, boolean isBoolean, List<String> nodeIds,
            List<GatherResult> gatherResults
        ) {
            this.name = name;
            this.clientId = clientId;
            this.requestId = requestId;
            this.afterDelay = afterDelay;
            this.isBoolean = isBoolean;
            this.nodeIds = nodeIds;
            this.gatherResults = gatherResults;
        }

    }

    public static class GatherResult
        implements Serializable, Comparable<GatherResult> {

        @XmlAttribute
        public String sensorId;

        @XmlAttribute
        public String nodeId;

        @XmlAttribute
        public double value;

        /**
         * Compares this GatherResult with another GatherResult based on the sensorId.
         *
         * @param o The other GatherResult to compare against.
         * @return An int representing the order of this result relative to the other result.
         */
        @Override
        public int compareTo(GatherResult o) {
            return sensorId.compareTo(o.sensorId);
        }

        public GatherResult() { }

        /**
         * Constructs a GatherResult with specified attributes.
         *
         * @param sensorId The sensor ID associated with this result.
         * @param nodeId The node ID associated with this result.
         * @param value The value recorded by the sensor.
         */
        public GatherResult(String sensorId, String nodeId, double value) {
            this.sensorId = sensorId;
            this.nodeId = nodeId;
            this.value = value;
        }

    }

}
