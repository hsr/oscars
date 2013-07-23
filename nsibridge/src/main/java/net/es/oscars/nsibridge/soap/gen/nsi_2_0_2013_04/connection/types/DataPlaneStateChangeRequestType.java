
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Type definition for the data plane state change notification
 *                 message.
 *                 
 *                 This notification message sent up from a PA when a data plane
 *                 status has changed. Possible data plane status changes are:
 *                 activation, deactivation and activation version change.
 *                 
 *                 Elements:
 *                 
 *                 dataPlaneStatus - Current data plane activation state for the
 *                 reservation identified by connectionId.
 *             
 * 
 * <p>Java class for DataPlaneStateChangeRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataPlaneStateChangeRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.ogf.org/nsi/2013/04/connection/types}NotificationBaseType">
 *       &lt;sequence>
 *         &lt;element name="dataPlaneStatus" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}DataPlaneStatusType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataPlaneStateChangeRequestType", propOrder = {
    "dataPlaneStatus"
})
public class DataPlaneStateChangeRequestType
    extends NotificationBaseType
{

    @XmlElement(required = true)
    protected DataPlaneStatusType dataPlaneStatus;

    /**
     * Gets the value of the dataPlaneStatus property.
     * 
     * @return
     *     possible object is
     *     {@link DataPlaneStatusType }
     *     
     */
    public DataPlaneStatusType getDataPlaneStatus() {
        return dataPlaneStatus;
    }

    /**
     * Sets the value of the dataPlaneStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataPlaneStatusType }
     *     
     */
    public void setDataPlaneStatus(DataPlaneStatusType value) {
        this.dataPlaneStatus = value;
    }

}
