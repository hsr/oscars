
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.discovery.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.TypeValuePairType;


/**
 * 
 *                 Type definition for the set of zero or more capabilities
 *                 supported by the version of the service.
 *                 
 *                 Elements:
 *                 
 *                 capability - A specific capability supported by the version
 *                 of the service.                
 *             
 * 
 * <p>Java class for CapabilitiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CapabilitiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="capability" type="{http://schemas.ogf.org/nsi/2012/03/framework/types}TypeValuePairType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CapabilitiesType", propOrder = {
    "capability"
})
public class CapabilitiesType {

    protected List<TypeValuePairType> capability;

    /**
     * Gets the value of the capability property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the capability property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCapability().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeValuePairType }
     * 
     * 
     */
    public List<TypeValuePairType> getCapability() {
        if (capability == null) {
            capability = new ArrayList<TypeValuePairType>();
        }
        return this.capability;
    }

}
