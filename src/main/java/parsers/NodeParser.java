package parsers;

import sensor_network.PortName;

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

public class NodeParser {

    public static ArrayList<Node> parse(File file) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(Forest.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        return ((Forest) unmarshaller.unmarshal(file)).nodes;
    }

    @XmlRootElement(name="forest")
    public static class Forest
        implements Serializable {

        @XmlElement(name="node")
        public ArrayList<Node> nodes;

    }

    public static class Node
        implements Serializable {

        @XmlElement
        public String id;

        @XmlElement
        public String pluginUri;

        @XmlElement
        public Integer range;

        @XmlElement
        public Position position;

        @XmlElement
        public Long startAfter;

        @XmlElement
        public Long endAfter;

        @XmlElement
        public Long sensorUpdateDelay;

        @XmlElement(name="sensor")
        @XmlElementWrapper(name="sensors")
        public List<Sensor> sensors;

        @XmlElement(name="port")
        @XmlElementWrapper(name="inboundPorts")
        public List<Port> inboundPorts;

        @XmlElement(name="port")
        @XmlElementWrapper(name="outboundPorts")
        public List<Port> outboundPorts;

    }

    public static class Position
        implements Serializable {

        @XmlAttribute
        public Integer x;

        @XmlAttribute
        public Integer y;

    }

    public static class Sensor
        implements Serializable {

        @XmlAttribute
        public String id;

        @XmlAttribute
        public Float value;

        @XmlAttribute
        public Float toAdd;

    }

    public static class Port
        implements Serializable {

        @XmlAttribute(name="for")
        public PortName portName;

        @XmlAttribute
        public String uri;

    }

}
