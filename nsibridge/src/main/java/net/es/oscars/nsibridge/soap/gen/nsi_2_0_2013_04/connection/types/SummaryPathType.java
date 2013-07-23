
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 This type is used to model a connection reservation's summary
 *                 path information.  The structure provides the next level of
 *                 connection information but not state.
 * 
 *                 Attributes:
 *                 
 *                 order - Specification of ordered path elements.
 *                 
 *                 Elements:
 * 
 *                 connectionId - The connection identifier associated with the
 *                 reservation and path segment.
 *                 
 *                 providerNSA - The provider NSA holding the connection
 *                 information associated with this instance of data.
 *                 
 *                 path - The summary path information associated with the
 *                 connection reservation.
 *             
 * 
 * <p>Java class for SummaryPathType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SummaryPathType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2013/04/framework/types}ConnectionIdType"/>
 *         &lt;element name="providerNSA" type="{http://schemas.ogf.org/nsi/2013/04/framework/types}NsaIdType"/>
 *         &lt;element name="path" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}PathType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="order" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummaryPathType", propOrder = {
    "connectionId",
    "providerNSA",
    "path"
})
public class SummaryPathType {

    @XmlElement(required = true)
    protected String connectionId;
    @XmlElement(required = true)
    protected String providerNSA;
    protected PathType path;
    @XmlAttribute(required = true)
    protected int order;

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
     * Gets the value of the providerNSA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderNSA() {
        return providerNSA;
    }

    /**
     * Sets the value of the providerNSA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderNSA(String value) {
        this.providerNSA = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link PathType }
     *     
     */
    public PathType getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathType }
     *     
     */
    public void setPath(PathType value) {
        this.path = value;
    }

    /**
     * Gets the value of the order property.
     * 
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     */
    public void setOrder(int value) {
        this.order = value;
    }

}
