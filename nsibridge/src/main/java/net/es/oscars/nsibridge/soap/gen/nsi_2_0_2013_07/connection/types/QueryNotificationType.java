
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Type definition for the QueryNotification message providing a
 *                 mechanism for a Requester NSA to query a Provider NSA for a
 *                 set of notifications against a specific connectionId.
 *                 
 *                 Elements compose a filter for specifying the notifications to
 *                 return in response to the query operation.  The filter query
 *                 provides an inclusive range of notification identifiers based
 *                 on connectionId.
 *                 
 *                 Elements:
 *                 
 *                 connectionId - Notifications for this connectionId.
 *                 
 *                 startNotificationId - The start of the range of notificationIds
 *                 to return.  If not present then the query should start from
 *                 oldest notificationId available.
 *                 
 *                 endNotificationId - The end of the range of notificationIds
 *                 to return.  If not present then the query should end with
 *                 the newest notificationId available.
 *             
 * 
 * <p>Java class for QueryNotificationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryNotificationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2013/07/framework/types}ConnectionIdType"/>
 *         &lt;element name="startNotificationId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="endNotificationId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryNotificationType", propOrder = {
    "connectionId",
    "startNotificationId",
    "endNotificationId"
})
public class QueryNotificationType {

    @XmlElement(required = true)
    protected String connectionId;
    protected Integer startNotificationId;
    protected Integer endNotificationId;

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
     * Gets the value of the startNotificationId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStartNotificationId() {
        return startNotificationId;
    }

    /**
     * Sets the value of the startNotificationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStartNotificationId(Integer value) {
        this.startNotificationId = value;
    }

    /**
     * Gets the value of the endNotificationId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEndNotificationId() {
        return endNotificationId;
    }

    /**
     * Sets the value of the endNotificationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setEndNotificationId(Integer value) {
        this.endNotificationId = value;
    }

}
