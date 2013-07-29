
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Type definition for the reserveResponse message. A Provider NSA
 *                 sends this reserveResponse message immediately after receiving
 *                 the reservation request to inform the Requester NSA of the
 *                 connectionId allocated to their reservation request.  This
 *                 connectionId can then be used to query reservation progress.
 *                 
 *                 Elements:
 *                 
 *                 connectionId - The Provider NSA assigned connectionId for this
 *                 reservation request. This value will be unique within the context
 *                 of the Provider NSA.
 *             
 * 
 * <p>Java class for ReserveResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReserveResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2013/07/framework/types}ConnectionIdType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReserveResponseType", propOrder = {
    "connectionId"
})
public class ReserveResponseType {

    @XmlElement(required = true)
    protected String connectionId;

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
