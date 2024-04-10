package parsers;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.ArrayList;

public class ClientParser {

    public static ArrayList<Client> parse(File file) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(Clients.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        return ((Clients) unmarshaller.unmarshal(file)).clients;
    }

    @XmlRootElement(name="clients")
    public static class Clients {

        @XmlElement(name="client")
        ArrayList<Client> clients;

    }

    public static class Client {

        @XmlElement
        public String id;

        @XmlElement(name="target")
        @XmlElementWrapper(name="targets")
        public ArrayList<Target> targets;

        @XmlElement(name="frequency")
        public Integer frequency;

    }

    public static class Target {

        @XmlElement
        public String nodeId;

        @XmlElement
        public String port;

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
                   ", port='" + port + '\'' +
                   ", async=" + async +
                   ", query='" + query + '\'' +
                   ", initialDelay=" + initialDelay +
                   '}';
        }

    }

}
