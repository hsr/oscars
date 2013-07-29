
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 This type is used to model a connection reservation's detailed 
 *                 path information.  The structure is recursive so it is 
 *                 possible to model both an ordered list of connection segments,
 *                 as well as the hierarchical connection segments created on 
 *                 children NSA in either a tree and chain configuration.
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
 *                 connectionStates - This reservation's segments connection
 *                 states.
 *                 
 *                 criteria - A set of versioned reservation criteria information.
 *             
 * 
 * <p>Java class for ChildRecursiveType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChildRecursiveType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2013/07/framework/types}ConnectionIdType"/>
 *         &lt;element name="providerNSA" type="{http://schemas.ogf.org/nsi/2013/07/framework/types}NsaIdType"/>
 *         &lt;element name="connectionStates" type="{http://schemas.ogf.org/nsi/2013/07/connection/types}ConnectionStatesType"/>
 *         &lt;element name="criteria" type="{http://schemas.ogf.org/nsi/2013/07/connection/types}QueryRecursiveResultCriteriaType" maxOccurs="unbounded"/>
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
@XmlType(name = "ChildRecursiveType", propOrder = {
    "connectionId",
    "providerNSA",
    "connectionStates",
    "criteria"
})
public class ChildRecursiveType {

    @XmlElement(required = true)
    protected String connectionId;
    @XmlElement(required = true)
    protected String providerNSA;
    @XmlElement(required = true)
    protected ConnectionStatesType connectionStates;
    @XmlElement(required = true)
    protected List<QueryRecursiveResultCriteriaType> criteria;
    @XmlAttribute(name = "order", required = true)
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
     * Gets the value of the connectionStates property.
     * 
     * @return
     *     possible object is
     *     {@link ConnectionStatesType }
     *     
     */
    public ConnectionStatesType getConnectionStates() {
        return connectionStates;
    }

    /**
     * Sets the value of the connectionStates property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConnectionStatesType }
     *     
     */
    public void setConnectionStates(ConnectionStatesType value) {
        this.connectionStates = value;
    }

    /**
     * Gets the value of the criteria property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the criteria property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCriteria().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QueryRecursiveResultCriteriaType }
     * 
     * 
     */
    public List<QueryRecursiveResultCriteriaType> getCriteria() {
        if (criteria == null) {
            criteria = new ArrayList<QueryRecursiveResultCriteriaType>();
        }
        return this.criteria;
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
