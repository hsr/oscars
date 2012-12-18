
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProvisionStateEnumType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ProvisionStateEnumType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Initial"/>
 *     &lt;enumeration value="Scheduled"/>
 *     &lt;enumeration value="Provisioning"/>
 *     &lt;enumeration value="Provisioned"/>
 *     &lt;enumeration value="ProvisionFailed"/>
 *     &lt;enumeration value="Releasing"/>
 *     &lt;enumeration value="ReleaseFailed"/>
 *     &lt;enumeration value="Terminating"/>
 *     &lt;enumeration value="TerminatedReserved"/>
 *     &lt;enumeration value="TerminatedEndtime"/>
 *     &lt;enumeration value="TerminatedRequest"/>
 *     &lt;enumeration value="TerminateFailed"/>
 *     &lt;enumeration value="Unknown"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ProvisionStateEnumType")
@XmlEnum
public enum ProvisionStateEnumType {

    @XmlEnumValue("Initial")
    INITIAL("Initial"),
    @XmlEnumValue("Scheduled")
    SCHEDULED("Scheduled"),
    @XmlEnumValue("Provisioning")
    PROVISIONING("Provisioning"),
    @XmlEnumValue("Provisioned")
    PROVISIONED("Provisioned"),
    @XmlEnumValue("ProvisionFailed")
    PROVISION_FAILED("ProvisionFailed"),
    @XmlEnumValue("Releasing")
    RELEASING("Releasing"),
    @XmlEnumValue("ReleaseFailed")
    RELEASE_FAILED("ReleaseFailed"),
    @XmlEnumValue("Terminating")
    TERMINATING("Terminating"),
    @XmlEnumValue("TerminatedReserved")
    TERMINATED_RESERVED("TerminatedReserved"),
    @XmlEnumValue("TerminatedEndtime")
    TERMINATED_ENDTIME("TerminatedEndtime"),
    @XmlEnumValue("TerminatedRequest")
    TERMINATED_REQUEST("TerminatedRequest"),
    @XmlEnumValue("TerminateFailed")
    TERMINATE_FAILED("TerminateFailed"),
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown");
    private final String value;

    ProvisionStateEnumType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProvisionStateEnumType fromValue(String v) {
        for (ProvisionStateEnumType c: ProvisionStateEnumType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
