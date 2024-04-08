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

        public String id;

        @XmlElement(name="query")
        @XmlElementWrapper(name="queries")
        public ArrayList<String> queries;

        @XmlElement(name="nodeId")
        @XmlElementWrapper(name="targetNodes")
        public ArrayList<String> targetNodes;

        @XmlElement(name="frequency")
        public Integer frequency;

    }

}
