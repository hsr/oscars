
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for QueryOperationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="QueryOperationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Summary"/>
 *     &lt;enumeration value="Details"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "QueryOperationType")
@XmlEnum
public enum QueryOperationType {

    @XmlEnumValue("Summary")
    SUMMARY("Summary"),
    @XmlEnumValue("Details")
    DETAILS("Details");
    private final String value;

    QueryOperationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static QueryOperationType fromValue(String v) {
        for (QueryOperationType c: QueryOperationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
