
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 A filter for specifying the reservations to return in response to a
 *                 queryRequest.
 * 
 *                 Supports querying reservations based on connectionId or
 *                 globalReservationId. Filter items specified are OR'ed to build
 *                 the match criteria. If no criteria is specified then all
 *                 reservations associated with the requesting NSA are returned.
 *          
 * 
 * <p>Java class for QueryFilterType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryFilterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ConnectionIdType" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="globalReservationId" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}GlobalReservationIdType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryFilterType", propOrder = {
    "connectionId",
    "globalReservationId"
})
public class QueryFilterType {

    protected List<String> connectionId;
    protected List<String> globalReservationId;

    /**
     * Gets the value of the connectionId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the connectionId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConnectionId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getConnectionId() {
        if (connectionId == null) {
            connectionId = new ArrayList<String>();
        }
        return this.connectionId;
    }

    /**
     * Gets the value of the globalReservationId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the globalReservationId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGlobalReservationId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getGlobalReservationId() {
        if (globalReservationId == null) {
            globalReservationId = new ArrayList<String>();
        }
        return this.globalReservationId;
    }

}
