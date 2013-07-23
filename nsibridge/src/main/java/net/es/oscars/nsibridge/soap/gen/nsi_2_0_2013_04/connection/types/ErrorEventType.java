
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.types.ServiceExceptionType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.types.TypeValuePairListType;


/**
 * 
 *                 Type definition for an autonomous message issued from a
 *                 Provider NSA to a Requester NSA when an existing reservation
 *                 encounters an autonomous error condition such as being
 *                 administratively terminated before the reservation's scheduled
 *                 end-time.
 *                 
 *                 Elements:
 *                 
 *                 event - The type of event that generated this notification.
 *                 
 *                 additionalInfo - Type/value pairs that can provide additional
 *                 error context as needed.
 *                 
 *                 serviceException - Specific error condition - the reason for the
 *                 generation of the error event.
 *             
 * 
 * <p>Java class for ErrorEventType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ErrorEventType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.ogf.org/nsi/2013/04/connection/types}NotificationBaseType">
 *       &lt;sequence>
 *         &lt;element name="event" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}EventEnumType"/>
 *         &lt;element name="additionalInfo" type="{http://schemas.ogf.org/nsi/2013/04/framework/types}TypeValuePairListType" minOccurs="0"/>
 *         &lt;element name="serviceException" type="{http://schemas.ogf.org/nsi/2013/04/framework/types}ServiceExceptionType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ErrorEventType", propOrder = {
    "event",
    "additionalInfo",
    "serviceException"
})
public class ErrorEventType
    extends NotificationBaseType
{

    @XmlElement(required = true)
    protected EventEnumType event;
    protected TypeValuePairListType additionalInfo;
    protected ServiceExceptionType serviceException;

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
