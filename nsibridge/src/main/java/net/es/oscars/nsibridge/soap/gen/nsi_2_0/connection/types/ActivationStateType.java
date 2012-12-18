
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Models the current connection activation state within the
 *                 data plane.
 *                 
 *                 Attributes:
 *                 
 *                 version - Version of the connection reservation this entry is
 *                 modeling.
 *                 
 *                 Elements:
 *                 
 *                 state - The state of the connection activation state machine.                  
 *             
 * 
 * <p>Java class for ActivationStateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ActivationStateType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="state" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ActivationStateEnumType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivationStateType", propOrder = {
    "state"
})
public class ActivationStateType {

    @XmlElement(required = true)
    protected ActivationStateEnumType state;
    @XmlAttribute
    protected Integer version;

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link ActivationStateEnumType }
     *     
     */
    public ActivationStateEnumType getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivationStateEnumType }
     *     
     */
    public void setState(ActivationStateEnumType value) {
        this.state = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setVersion(Integer value) {
        this.version = value;
    }

}
