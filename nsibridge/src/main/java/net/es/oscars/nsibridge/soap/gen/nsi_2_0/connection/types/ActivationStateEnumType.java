
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ActivationStateEnumType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ActivationStateEnumType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Inactive"/>
 *     &lt;enumeration value="Activating"/>
 *     &lt;enumeration value="Active"/>
 *     &lt;enumeration value="Deactivating"/>
 *     &lt;enumeration value="Unknown"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ActivationStateEnumType")
@XmlEnum
public enum ActivationStateEnumType {

    @XmlEnumValue("Inactive")
    INACTIVE("Inactive"),
    @XmlEnumValue("Activating")
    ACTIVATING("Activating"),
    @XmlEnumValue("Active")
    ACTIVE("Active"),
    @XmlEnumValue("Deactivating")
    DEACTIVATING("Deactivating"),
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown");
    private final String value;

    ActivationStateEnumType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ActivationStateEnumType fromValue(String v) {
        for (ActivationStateEnumType c: ActivationStateEnumType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
