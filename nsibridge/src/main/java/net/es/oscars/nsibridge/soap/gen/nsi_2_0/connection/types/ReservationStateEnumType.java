
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

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
 *     &lt;enumeration value="Initial"/>
 *     &lt;enumeration value="Reserving"/>
 *     &lt;enumeration value="Reserved"/>
 *     &lt;enumeration value="ReserveFailed"/>
 *     &lt;enumeration value="ModifyChecking"/>
 *     &lt;enumeration value="ModifyChecked"/>
 *     &lt;enumeration value="ModifyCheckFailed"/>
 *     &lt;enumeration value="ModifyCanceling"/>
 *     &lt;enumeration value="ModifyCancelFailed"/>
 *     &lt;enumeration value="Modifying"/>
 *     &lt;enumeration value="ModifyFailed"/>
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
@XmlType(name = "ReservationStateEnumType")
@XmlEnum
public enum ReservationStateEnumType {

    @XmlEnumValue("Initial")
    INITIAL("Initial"),
    @XmlEnumValue("Reserving")
    RESERVING("Reserving"),
    @XmlEnumValue("Reserved")
    RESERVED("Reserved"),
    @XmlEnumValue("ReserveFailed")
    RESERVE_FAILED("ReserveFailed"),
    @XmlEnumValue("ModifyChecking")
    MODIFY_CHECKING("ModifyChecking"),
    @XmlEnumValue("ModifyChecked")
    MODIFY_CHECKED("ModifyChecked"),
    @XmlEnumValue("ModifyCheckFailed")
    MODIFY_CHECK_FAILED("ModifyCheckFailed"),
    @XmlEnumValue("ModifyCanceling")
    MODIFY_CANCELING("ModifyCanceling"),
    @XmlEnumValue("ModifyCancelFailed")
    MODIFY_CANCEL_FAILED("ModifyCancelFailed"),
    @XmlEnumValue("Modifying")
    MODIFYING("Modifying"),
    @XmlEnumValue("ModifyFailed")
    MODIFY_FAILED("ModifyFailed"),
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
