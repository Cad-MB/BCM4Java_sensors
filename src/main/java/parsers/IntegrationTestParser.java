package parsers;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;

public class IntegrationTestParser {

    public static ArrayList<Test> parse(File file) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(NodeParser.Forest.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        return ((Tests) unmarshaller.unmarshal(file)).tests;
    }

    @XmlRootElement(name="tests")
    public static class Tests {

        @XmlElement(name="test")
        public ArrayList<Test> tests;

    }

    public static class Test {

        @XmlElement
        public String clientId;

        @XmlElement
        public String requestId;

        @XmlElement
        public Instant instant;

        @XmlElement
        public int captureDelay;

        @XmlElement
        public boolean isBoolean;

        @XmlElement(name="nodeId")
        @XmlElementWrapper(name="expectBoolean")
        public ArrayList<String> nodeIds;

        @XmlElement(name="sensor")
        @XmlElementWrapper(name="expectGather")
        public ArrayList<GatherResult> gatherResults;

    }

    public static class GatherResult {

        @XmlAttribute
        public String sensorId;

        @XmlAttribute
        public String nodeId;

        @XmlAttribute
        public double value;

    }

}
