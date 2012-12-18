
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Type definition for the reserve message that allows a
 *                 Requester NSA to reserve network resources for a connection
 *                 between two STP's constrained by a certain service parameters.
 * 
 *                 Elements:
 *                 
 *                 globalReservationId - An optional global reservation id that can be
 *                 used to correlate individual related service reservations through
 *                 the network. This must be populated with a Universally Unique
 *                 Identifier (UUID) URN as per ITU-T Rec. X.667 | ISO/IEC 9834-8:2005
 *                 and IETF RFC 4122.
 *                 
 *                 description - An optional description for the service reservation.
 *                 
 *                 connectionId - The Requester NSA assigned connectionId for this
 *                 service segment. This value must be unique within the context
 *                 of the Requester and Provider NSA.  This must be populated with a
 *                 Universally Unique Identifier (UUID) URN as per ITU-T Rec. X.667 |
 *                 ISO/IEC 9834-8:2005 and IETF RFC 4122.
 *                 
 *                 criteria - reservation request criteria including start and end
 *                 time, severice attributes, and requested path for the service.
 *             
 * 
 * <p>Java class for ReserveType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReserveType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="globalReservationId" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}GlobalReservationIdType" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ConnectionIdType"/>
 *         &lt;element name="criteria" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ReservationRequestCriteriaType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReserveType", propOrder = {
    "globalReservationId",
    "description",
    "connectionId",
    "criteria"
})
public class ReserveType {

    protected String globalReservationId;
    protected String description;
    @XmlElement(required = true)
    protected String connectionId;
    @XmlElement(required = true)
    protected ReservationRequestCriteriaType criteria;

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
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
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

    /**
     * Gets the value of the criteria property.
     * 
     * @return
     *     possible object is
     *     {@link ReservationRequestCriteriaType }
     *     
     */
    public ReservationRequestCriteriaType getCriteria() {
        return criteria;
    }

    /**
     * Sets the value of the criteria property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReservationRequestCriteriaType }
     *     
     */
    public void setCriteria(ReservationRequestCriteriaType value) {
        this.criteria = value;
    }

}
