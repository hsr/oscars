
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 A holder element providing an envelope that will contain the
 *                 list of child NSA and associated detailed connection
 *                 information.
 *                 
 *                 Elements:
 *                 
 *                 child - Detailed path information for a child NSA.  Each
 *                 child element is ordered and contains a connection segment in
 *                 the overall path.
 *             
 * 
 * <p>Java class for ChildRecursiveListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChildRecursiveListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="child" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}RecursivePathType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChildRecursiveListType", propOrder = {
    "child"
})
public class ChildRecursiveListType {

    protected List<RecursivePathType> child;

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
     * {@link RecursivePathType }
     * 
     * 
     */
    public List<RecursivePathType> getChild() {
        if (child == null) {
            child = new ArrayList<RecursivePathType>();
        }
        return this.child;
    }

}
