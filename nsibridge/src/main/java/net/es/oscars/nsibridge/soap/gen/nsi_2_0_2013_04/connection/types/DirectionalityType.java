
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DirectionalityType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DirectionalityType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Bidirectional"/>
 *     &lt;enumeration value="Unidirectional"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DirectionalityType")
@XmlEnum
public enum DirectionalityType {

    @XmlEnumValue("Bidirectional")
    BIDIRECTIONAL("Bidirectional"),
    @XmlEnumValue("Unidirectional")
    UNIDIRECTIONAL("Unidirectional");
    private final String value;

    DirectionalityType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DirectionalityType fromValue(String v) {
        for (DirectionalityType c: DirectionalityType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
