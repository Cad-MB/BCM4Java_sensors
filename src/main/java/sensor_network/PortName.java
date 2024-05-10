package sensor_network;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum PortName {
    @XmlEnumValue("request-result") REQUEST_RESULT,
    @XmlEnumValue("lookup") LOOKUP,
    @XmlEnumValue("clock") CLOCK,
    @XmlEnumValue("p2p") P2P,
    @XmlEnumValue("requesting") REQUESTING,
    @XmlEnumValue("registration") REGISTRATION;

    public String xmlName() {
        return name().replace("_", "-").toLowerCase();

    }
}
