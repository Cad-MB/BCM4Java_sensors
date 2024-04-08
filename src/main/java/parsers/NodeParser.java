package parsers;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NodeParser {

    public static ArrayList<Node> parse(File file) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(Forest.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        return ((Forest) unmarshaller.unmarshal(file)).nodes;
    }

    @XmlRootElement(name="forest")
    public static class Forest {

        @XmlElement(name="node")
        public ArrayList<Node> nodes;

    }

    public static class Node {

        @XmlElement
        public String id;

        @XmlElement
        public Integer range;

        @XmlElement
        public Position position;

        @XmlElement
        public Integer delay;

        @XmlElement(name="sensor")
        @XmlElementWrapper(name="sensors")
        public List<Sensor> sensors;

    }

    public static class Position {

        @XmlAttribute
        public Integer x;

        @XmlAttribute
        public Integer y;

    }

    public static class Sensor {

        @XmlAttribute
        public String id;

        @XmlAttribute
        public Integer value;

        @XmlAttribute
        public Integer toAdd;

    }

}