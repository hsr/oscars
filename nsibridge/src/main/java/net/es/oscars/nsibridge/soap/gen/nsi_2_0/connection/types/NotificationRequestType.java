
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.TypeValuePairListType;


/**
 * 
 *                 Type definition for the notification message that allows a
 *                 Requester NSA to reserve network resources for a connection
 *                 between two STP's constrained by a certain service parameters.
 *                 
 *                 Elements:
 *                 
 *                 event - The type of notification event generated
 *                 
 *                 connectionId - The unique identifier for a connection known
 *                 between a Requesting and Provider NSA pair.
 *                 
 *                 connectionStates - Connection state for the reservation.
 *                 
 *                 timeStamp - Time the event was generated.
 *                 
 *                 additionalInfo - Type/value 
 *             
 * 
 * <p>Java class for NotificationRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NotificationRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="event" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}EventEnumType"/>
 *         &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ConnectionIdType"/>
 *         &lt;element name="connectionStates" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ConnectionStatesType"/>
 *         &lt;element name="timeStamp" type="{http://schemas.ogf.org/nsi/2012/03/framework/types}DateTimeType"/>
 *         &lt;element name="additionalInfo" type="{http://schemas.ogf.org/nsi/2012/03/framework/types}TypeValuePairListType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NotificationRequestType", propOrder = {
    "event",
    "connectionId",
    "connectionStates",
    "timeStamp",
    "additionalInfo"
})
public class NotificationRequestType {

    @XmlElement(required = true)
    protected EventEnumType event;
    @XmlElement(required = true)
    protected String connectionId;
    @XmlElement(required = true)
    protected ConnectionStatesType connectionStates;
    @XmlElement(required = true)
    protected XMLGregorianCalendar timeStamp;
    @XmlElement(required = true)
    protected TypeValuePairListType additionalInfo;

    /**
     * Gets the value of the event property.
     * 
     * @return
     *     possible object is
     *     {@link EventEnumType }
     *     
     */
    public EventEnumType getEvent() {
        return event;
    }

    /**
     * Sets the value of the event property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventEnumType }
     *     
     */
    public void setEvent(EventEnumType value) {
        this.event = value;
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
     * Gets the value of the timeStamp property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the value of the timeStamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeStamp(XMLGregorianCalendar value) {
        this.timeStamp = value;
    }

    /**
     * Gets the value of the additionalInfo property.
     * 
     * @return
     *     possible object is
     *     {@link TypeValuePairListType }
     *     
     */
    public TypeValuePairListType getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Sets the value of the additionalInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeValuePairListType }
     *     
     */
    public void setAdditionalInfo(TypeValuePairListType value) {
        this.additionalInfo = value;
    }

}
