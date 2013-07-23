
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 A Service Termination Point (STP) that can be ordered in a list for
 *                 use in PathObject definition.
 *     
 *                 Attributes:
 *     
 *                 order - Order attribute is provided only when the STP is part of an
 *                 orderedStpList.
 *     
 *                 Elements:
 *     
 *                 stp - The Service Termination Point (STP).
 *             
 * 
 * <p>Java class for OrderedStpType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OrderedStpType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="stp" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}StpType"/>
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
@XmlType(name = "OrderedStpType", propOrder = {
    "stp"
})
public class OrderedStpType {

    @XmlElement(required = true)
    protected StpType stp;
    @XmlAttribute(name = "order", required = true)
    protected int order;

    /**
     * Gets the value of the stp property.
     * 
     * @return
     *     possible object is
     *     {@link StpType }
     *     
     */
    public StpType getStp() {
        return stp;
    }

    /**
     * Sets the value of the stp property.
     * 
     * @param value
     *     allowed object is
     *     {@link StpType }
     *     
     */
    public void setStp(StpType value) {
        this.stp = value;
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
