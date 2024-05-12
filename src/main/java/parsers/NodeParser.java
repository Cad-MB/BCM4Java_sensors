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

    /**
     * Parses the XML file into a list of Node objects.
     *
     * @param file The XML file containing the node configurations.
     * @return ArrayList of Node objects parsed from the file.
     * @throws Exception If there is an error during the parsing process.
     */
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
        public Threads threads;

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

        public Node() { }

        /**
         * Constructs a Node with specified attributes.
         *
         * @param id The unique identifier of the node.
         * @param pluginUri The URI of the plugin used by the node.
         * @param threads The thread configuration for the node.
         * @param range The communication range of the node.
         * @param position The position of the node.
         * @param startAfter The time after which the node will start operations.
         * @param endAfter The time after which the node will end operations.
         * @param sensorUpdateDelay The delay between sensor updates.
         * @param sensors The list of sensors attached to the node.
         * @param inboundPorts The list of inbound ports for the node.
         * @param outboundPorts The list of outbound ports for the node.
         */
        public Node(
            String id, String pluginUri, Threads threads, Integer range, Position position, Long startAfter, Long endAfter,
            Long sensorUpdateDelay, List<Sensor> sensors, List<Port> inboundPorts, List<Port> outboundPorts
        ) {
            this.id = id;
            this.pluginUri = pluginUri;
            this.threads = threads;
            this.range = range;
            this.position = position;
            this.startAfter = startAfter;
            this.endAfter = endAfter;
            this.sensorUpdateDelay = sensorUpdateDelay;
            this.sensors = sensors;
            this.inboundPorts = inboundPorts;
            this.outboundPorts = outboundPorts;
        }

    }

    public static class Threads {

        @XmlAttribute
        public Integer nbThreads;

        @XmlAttribute
        public Integer nbScheduleThreads;

        public Threads() { }

        /**
         * Constructs Threads with specified attributes.
         *
         * @param nbThreads The number of threads.
         * @param nbScheduleThreads The number of scheduled threads.
         */
        public Threads(Integer nbThreads, Integer nbScheduleThreads) {
            this.nbThreads = nbThreads;
            this.nbScheduleThreads = nbScheduleThreads;
        }

    }

    public static class Position
        implements Serializable {

        @XmlAttribute
        public Integer x;

        @XmlAttribute
        public Integer y;

        public Position() { }

        /**
         * Constructs a Position with specified coordinates.
         *
         * @param x The x-coordinate of the position.
         * @param y The y-coordinate of the position.
         */
        public Position(Integer x, Integer y) {
            this.x = x;
            this.y = y;
        }

    }

    public static class Sensor
        implements Serializable {

        @XmlAttribute
        public String id;

        @XmlAttribute
        public Float value;

        @XmlAttribute
        public Float toAdd;

        public Sensor() { }

        /**
         * Constructs a Sensor with specified attributes.
         *
         * @param id The identifier of the sensor.
         * @param value The initial value of the sensor.
         * @param toAdd The amount to add to the sensor value at each update.
         */
        public Sensor(String id, Float value, Float toAdd) {
            this.id = id;
            this.value = value;
            this.toAdd = toAdd;
        }

    }

    public static class Port
        implements Serializable {

        @XmlAttribute(name="for")
        public PortName portName;

        @XmlAttribute
        public String uri;

        public Port() { }

        /**
         * Constructs a Port with specified attributes.
         *
         * @param portName The name of the port.
         * @param uri The URI associated with the port.
         */
        public Port(PortName portName, String uri) {
            this.portName = portName;
            this.uri = uri;
        }

    }

}
