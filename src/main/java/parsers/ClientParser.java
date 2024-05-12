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

public class ClientParser {

    /**
     * Parses the XML file into a list of Client objects.
     *
     * @param file The XML file containing the client configurations.
     * @return ArrayList of Client objects parsed from the file.
     * @throws Exception If there is an error during the parsing process.
     */
    public static ArrayList<Client> parse(File file) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(Clients.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        return ((Clients) unmarshaller.unmarshal(file)).clients;
    }

    @XmlRootElement(name="clients")
    public static class Clients
        implements Serializable {

        @XmlElement(name="client")
        public ArrayList<Client> clients;

    }

    public static class Client
        implements Serializable {

        @XmlElement
        public String id;

        @XmlElement
        public String pluginUri;

        @XmlElement
        public NodeParser.Threads threads;

        @XmlElement
        public Integer requestTimeout;

        @XmlElement(name="target")
        @XmlElementWrapper(name="targets")
        public ArrayList<Target> targets;

        @XmlElement(name="frequency")
        public Integer frequency;

        @XmlElement
        public Integer endAfter;

        @XmlElement(name="port")
        @XmlElementWrapper(name="inboundPorts")
        public List<Port> inboundPorts;

        @XmlElement(name="port")
        @XmlElementWrapper(name="outboundPorts")
        public List<Port> outboundPorts;

        /**
         * Default constructor for Client.
         */
        public Client() { }

        /**
         * Constructs a Client with specified attributes.
         *
         * @param id The unique identifier of the client.
         * @param pluginUri The URI of the plugin used by the client.
         * @param requestTimeout The timeout for requests made by the client.
         * @param threads The thread configuration for the client.
         * @param targets The list of targets the client will interact with.
         * @param frequency The frequency of operations for the client.
         * @param endAfter The time after which the client will end its operations.
         * @param inboundPorts The list of inbound ports for the client.
         * @param outboundPorts The list of outbound ports for the client.
         */
        public Client(
            String id, String pluginUri, Integer requestTimeout, NodeParser.Threads threads, ArrayList<Target> targets, Integer frequency,
            Integer endAfter, List<Port> inboundPorts, List<Port> outboundPorts
        ) {
            this.id = id;
            this.pluginUri = pluginUri;
            this.requestTimeout = requestTimeout;
            this.threads = threads;
            this.targets = targets;
            this.frequency = frequency;
            this.endAfter = endAfter;
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

    public static class Target
        implements Serializable {

        @XmlElement
        public String nodeId;

        @XmlElement
        public String targetPort;

        @XmlElement
        public boolean async;

        @XmlElement
        public String query;

        @XmlElement
        public Integer initialDelay;

        @Override
        public String toString() {
            return "Target{" +
                   "nodeId='" + nodeId + '\'' +
                   ", targetPort='" + targetPort + '\'' +
                   ", async=" + async +
                   ", query='" + query + '\'' +
                   ", initialDelay=" + initialDelay +
                   '}';
        }

        public Target() { }

        /**
         * Constructs a Target with specified attributes.
         *
         * @param nodeId The node ID the target points to.
         * @param targetPort The target port at the node.
         * @param async Whether the operation is asynchronous.
         * @param query The query to be executed at the target.
         * @param initialDelay The initial delay before executing the operation.
         */
        public Target(String nodeId, String targetPort, boolean async, String query, Integer initialDelay) {
            this.nodeId = nodeId;
            this.targetPort = targetPort;
            this.async = async;
            this.query = query;
            this.initialDelay = initialDelay;
        }

    }

    public static class Port {

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
