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

    }

    public static class GatherResult
        implements Serializable, Comparable<GatherResult> {

        @XmlAttribute
        public String sensorId;

        @XmlAttribute
        public String nodeId;

        @XmlAttribute
        public double value;

        @Override
        public int compareTo(GatherResult o) {
            return sensorId.compareTo(o.sensorId);
        }

    }

}
