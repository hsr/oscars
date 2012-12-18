
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.TypeValuePairListType;


/**
 * 
 *                 The Service Termination Point (STP) type used for path selection.
 *                  
 *                 Elements:
 *                 
 *                 networkId - a globally unique identifier (URN) that identifies the
 *                 Network.  Rather than forcing parsing of an STP to determine the
 *                 Network, a separate Network object is defined to allow an
 *                 intermediate NSA to forward the message to the target Network
 *                 without needing to know about the STPs within that domain.
 *                 
 *                 localId - a locally unique identifier for the STP withint he
 *                 associated network.
 *                 
 *                 labels - technology specific attributes associated with
 *                 the Service Termination Point.
 *                 
 *                 orientation - describes the connection orientation of the STP in 
 *                 the context of the network (Ingress or Egress).  Required to
 *                 remove path-finding ambiguity for bidirectional STP.
 *             
 * 
 * <p>Java class for StpType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StpType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="networkId" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}NetworkIdType"/>
 *         &lt;element name="localId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="labels" type="{http://schemas.ogf.org/nsi/2012/03/framework/types}TypeValuePairListType" minOccurs="0"/>
 *         &lt;element name="orientation" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}OrientationType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StpType", propOrder = {
    "networkId",
    "localId",
    "labels",
    "orientation"
})
public class StpType {

    @XmlElement(required = true)
    protected String networkId;
    @XmlElement(required = true)
    protected String localId;
    protected TypeValuePairListType labels;
    protected OrientationType orientation;

    /**
     * Gets the value of the networkId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNetworkId() {
        return networkId;
    }

    /**
     * Sets the value of the networkId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNetworkId(String value) {
        this.networkId = value;
    }

    /**
     * Gets the value of the localId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalId() {
        return localId;
    }

    /**
     * Sets the value of the localId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalId(String value) {
        this.localId = value;
    }

    /**
     * Gets the value of the labels property.
     * 
     * @return
     *     possible object is
     *     {@link TypeValuePairListType }
     *     
     */
    public TypeValuePairListType getLabels() {
        return labels;
    }

    /**
     * Sets the value of the labels property.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeValuePairListType }
     *     
     */
    public void setLabels(TypeValuePairListType value) {
        this.labels = value;
    }

    /**
     * Gets the value of the orientation property.
     * 
     * @return
     *     possible object is
     *     {@link OrientationType }
     *     
     */
    public OrientationType getOrientation() {
        return orientation;
    }

    /**
     * Sets the value of the orientation property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrientationType }
     *     
     */
    public void setOrientation(OrientationType value) {
        this.orientation = value;
    }

}
