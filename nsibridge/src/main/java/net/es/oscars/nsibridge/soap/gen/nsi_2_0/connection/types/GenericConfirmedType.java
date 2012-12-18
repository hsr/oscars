
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 This is a generic type definition for a "Confirmed" messages in
 *                 response to a successful processing of a previous "Request"
 *                 message such as provision, release, and terminate.
 * 
 *                 Elements:
 * 
 *                 globalReservationId - An optional global reservation id that was
 *                 originally provided in the reserve request.
 * 
 *                 connectionId - The unique identifier for a connection that is known
 *                 between a Requesting and Provider NSA pair.
 *             
 * 
 * <p>Java class for GenericConfirmedType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GenericConfirmedType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="globalReservationId" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}GlobalReservationIdType" minOccurs="0"/>
 *         &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ConnectionIdType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenericConfirmedType", propOrder = {
    "globalReservationId",
    "connectionId"
})
public class GenericConfirmedType {

    protected String globalReservationId;
    @XmlElement(required = true)
    protected String connectionId;

    /**
     * Gets the value of the globalReservationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGlobalReservationId() {
        return globalReservationId;
    }

    /**
     * Sets the value of the globalReservationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlobalReservationId(String value) {
        this.globalReservationId = value;
    }

    /**
     * Gets the value of the connectionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * Sets the value of the connectionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionId(String value) {
        this.connectionId = value;
    }

}
