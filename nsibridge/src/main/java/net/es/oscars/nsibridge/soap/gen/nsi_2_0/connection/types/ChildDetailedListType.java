
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 A holder element providing an evelope which will contain the
 *                 list of child NSA and associated detailed connection
 *                 information.
 *                 
 *                 Elements:
 *                 
 *                 child - Deatiled path information for a child NSA.  Each
 *                 child element is ordered and contains a connection segment in
 *                 the overall path.
 *             
 * 
 * <p>Java class for ChildDetailedListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChildDetailedListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="child" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}DetailedPathType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChildDetailedListType", propOrder = {
    "child"
})
public class ChildDetailedListType {

    protected List<DetailedPathType> child;

    /**
     * Gets the value of the child property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the child property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChild().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DetailedPathType }
     * 
     * 
     */
    public List<DetailedPathType> getChild() {
        if (child == null) {
            child = new ArrayList<DetailedPathType>();
        }
        return this.child;
    }

}
