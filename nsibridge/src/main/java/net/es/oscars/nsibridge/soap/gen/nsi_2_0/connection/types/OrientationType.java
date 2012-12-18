
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OrientationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="OrientationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Ingress"/>
 *     &lt;enumeration value="Egress"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "OrientationType")
@XmlEnum
public enum OrientationType {

    @XmlEnumValue("Ingress")
    INGRESS("Ingress"),
    @XmlEnumValue("Egress")
    EGRESS("Egress");
    private final String value;

    OrientationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OrientationType fromValue(String v) {
        for (OrientationType c: OrientationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
