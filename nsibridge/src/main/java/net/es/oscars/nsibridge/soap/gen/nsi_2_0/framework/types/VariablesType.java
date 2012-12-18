
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 A specific type definition providing a set of zero or more
 *                 variables associated with the ServiceException.
 *             
 * 
 * <p>Java class for VariablesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VariablesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="variable" type="{http://schemas.ogf.org/nsi/2012/03/framework/types}TypeValuePairType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VariablesType", propOrder = {
    "variable"
})
public class VariablesType {

    protected List<TypeValuePairType> variable;

    /**
     * Gets the value of the variable property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the variable property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVariable().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeValuePairType }
     * 
     * 
     */
    public List<TypeValuePairType> getVariable() {
        if (variable == null) {
            variable = new ArrayList<TypeValuePairType>();
        }
        return this.variable;
    }

}
