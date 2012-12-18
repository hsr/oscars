
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Type definition for the modifyCheck message that allows a
 *                 Requester NSA to check the feasibility of connection reservation
 *                 modification.  Any additional resources associated with the
 *                 modify will be allocated.
 *                 
 *                 Elements:
 * 
 *                 connectionId - The Requester NSA assigned connectionId for this
 *                 service segment. This value must be unique within the context
 *                 of the Requester and Provider NSA.  This must be populated with a
 *                 Universally Unique Identifier (UUID) URN as per ITU-T Rec. X.667 |
 *                 ISO/IEC 9834-8:2005 and IETF RFC 4122.
 *                 
 *                 criteria - requested modification criteria including start and
 *                 end time, service attributes, and requested path for the
 *                 service.
 *             
 * 
 * <p>Java class for ModifyCheckType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ModifyCheckType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ConnectionIdType"/>
 *         &lt;element name="criteria" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ModifyRequestCriteriaType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModifyCheckType", propOrder = {
    "connectionId",
    "criteria"
})
public class ModifyCheckType {

    @XmlElement(required = true)
    protected String connectionId;
    @XmlElement(required = true)
    protected ModifyRequestCriteriaType criteria;

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
     *     {@link ModifyRequestCriteriaType }
     *     
     */
    public ModifyRequestCriteriaType getCriteria() {
        return criteria;
    }

    /**
     * Sets the value of the criteria property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModifyRequestCriteriaType }
     *     
     */
    public void setCriteria(ModifyRequestCriteriaType value) {
        this.criteria = value;
    }

}
