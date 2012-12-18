
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType;


/**
 * 
 *                 A generic "Failed" message type sent as request in response to a
 *                 failure to process a previous protocol "Request" message.  This is
 *                 used in response to all request types that can return an error.
 * 
 *                 Elements:
 * 
 *                 globalReservationId - An optional global reservation id that was
 *                 originally provided in the reserve request.
 * 
 *                 connectionId - The locally unique identifier for a connection that
 *                 is known between a Requesting and Provider NSA pair.
 * 
 *                 connectionStates - Overall connection state for the reservation.
 * 
 *                 ServiceException - Specific error condition - the reason for the
 *                 failure.
 *             
 * 
 * <p>Java class for GenericFailedType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GenericFailedType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="globalReservationId" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}GlobalReservationIdType" minOccurs="0"/>
 *         &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ConnectionIdType"/>
 *         &lt;element name="connectionStates" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ConnectionStatesType"/>
 *         &lt;element name="serviceException" type="{http://schemas.ogf.org/nsi/2012/03/framework/types}ServiceExceptionType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenericFailedType", propOrder = {
    "globalReservationId",
    "connectionId",
    "connectionStates",
    "serviceException"
})
public class GenericFailedType {

    protected String globalReservationId;
    @XmlElement(required = true)
    protected String connectionId;
    @XmlElement(required = true)
    protected ConnectionStatesType connectionStates;
    @XmlElement(required = true)
    protected ServiceExceptionType serviceException;

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

    /**
     * Gets the value of the connectionStates property.
     * 
     * @return
     *     possible object is
     *     {@link ConnectionStatesType }
     *     
     */
    public ConnectionStatesType getConnectionStates() {
        return connectionStates;
    }

    /**
     * Sets the value of the connectionStates property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConnectionStatesType }
     *     
     */
    public void setConnectionStates(ConnectionStatesType value) {
        this.connectionStates = value;
    }

    /**
     * Gets the value of the serviceException property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceExceptionType }
     *     
     */
    public ServiceExceptionType getServiceException() {
        return serviceException;
    }

    /**
     * Sets the value of the serviceException property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceExceptionType }
     *     
     */
    public void setServiceException(ServiceExceptionType value) {
        this.serviceException = value;
    }

}
