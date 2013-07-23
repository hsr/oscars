
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 This is the type definition for the querySummaryConfirmed message
 *                 (both synchronous and asynchronous versions). An NSA sends this
 *                 positive querySummaryRequest response to the NSA that issued the
 *                 original request message.  There can be zero or more results
 *                 retuned in this confirmed message depending on the number of
 *                 matching reservation results.
 * 
 *                 Elements:
 * 
 *                 reservation - Resulting summary set of connection reservations
 *                 matching the query criteria.
 * 
 *                 If there were no matches to the query then no reservation
 *                 elements will be present.
 *             
 * 
 * <p>Java class for QuerySummaryConfirmedType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QuerySummaryConfirmedType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="reservation" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}QuerySummaryResultType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuerySummaryConfirmedType", propOrder = {
    "reservation"
})
public class QuerySummaryConfirmedType {

    protected List<QuerySummaryResultType> reservation;

    /**
     * Gets the value of the reservation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reservation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReservation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QuerySummaryResultType }
     * 
     * 
     */
    public List<QuerySummaryResultType> getReservation() {
        if (reservation == null) {
            reservation = new ArrayList<QuerySummaryResultType>();
        }
        return this.reservation;
    }

}
