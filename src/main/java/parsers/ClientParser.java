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

    public static ArrayList<Client> parse(File file) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(Clients.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        return ((Clients) unmarshaller.unmarshal(file)).clients;
    }

    @XmlRootElement(name="clients")
    public static class Clients
        implements Serializable {

        @XmlElement(name="client")
        ArrayList<Client> clients;

    }

    public static class Client
        implements Serializable {

        @XmlElement
        public String id;

        @XmlElement
        public String pluginUri;

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

    }

    public static class Port {

        @XmlAttribute(name="for")
        public PortName portName;

        @XmlAttribute
        public String uri;

    }

}
