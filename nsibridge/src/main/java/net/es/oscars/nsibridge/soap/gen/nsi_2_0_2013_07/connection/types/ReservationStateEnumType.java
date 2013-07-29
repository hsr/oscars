
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReservationStateEnumType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ReservationStateEnumType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ReserveStart"/>
 *     &lt;enumeration value="ReserveChecking"/>
 *     &lt;enumeration value="ReserveFailed"/>
 *     &lt;enumeration value="ReserveAborting"/>
 *     &lt;enumeration value="ReserveHeld"/>
 *     &lt;enumeration value="ReserveCommitting"/>
 *     &lt;enumeration value="ReserveTimeout"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ReservationStateEnumType")
@XmlEnum
public enum ReservationStateEnumType {

    @XmlEnumValue("ReserveStart")
    RESERVE_START("ReserveStart"),
    @XmlEnumValue("ReserveChecking")
    RESERVE_CHECKING("ReserveChecking"),
    @XmlEnumValue("ReserveFailed")
    RESERVE_FAILED("ReserveFailed"),
    @XmlEnumValue("ReserveAborting")
    RESERVE_ABORTING("ReserveAborting"),
    @XmlEnumValue("ReserveHeld")
    RESERVE_HELD("ReserveHeld"),
    @XmlEnumValue("ReserveCommitting")
    RESERVE_COMMITTING("ReserveCommitting"),
    @XmlEnumValue("ReserveTimeout")
    RESERVE_TIMEOUT("ReserveTimeout");
    private final String value;

    ReservationStateEnumType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReservationStateEnumType fromValue(String v) {
        for (ReservationStateEnumType c: ReservationStateEnumType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
