
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 A notification message type definition for the Message Transport
 *                 Layer (MTL) delivery timeout of a request message. In the event
 *                 of an MTL timed out or Coordinator timeout, the Coordinator will
 *                 generate this message delivery failure notification and send it
 *                 up the workflow tree (towards the uRA).
 *                 
 *                 An MTL timeout can be generated as the result of a timeout
 *                 on receiving an ACK message for a corresponding send request.
 *                 A Coordinator timeout can occur when no confirm or fail reply
 *                 has been received to a previous request issued by the
 *                 Coordinator.  In both cases the local timers for these timeout
 *                 conditions are locally defined.
 *                 
 *                 Elements:
 *                 
 *                 correlationId - This value indicates the correlationId of
 *                 the original message that the transport layer failed to
 *                 send.
 *             
 * 
 * <p>Java class for MessageDeliveryTimeoutRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MessageDeliveryTimeoutRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.ogf.org/nsi/2013/07/connection/types}NotificationBaseType">
 *       &lt;sequence>
 *         &lt;element name="correlationId" type="{http://schemas.ogf.org/nsi/2013/07/framework/types}UuidType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageDeliveryTimeoutRequestType", propOrder = {
    "correlationId"
})
public class MessageDeliveryTimeoutRequestType
    extends NotificationBaseType
{

    @XmlElement(required = true)
    protected String correlationId;

    /**
     * Gets the value of the correlationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets the value of the correlationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorrelationId(String value) {
        this.correlationId = value;
    }

}
