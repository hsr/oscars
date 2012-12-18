
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 This type is used to model a connection reservation's detailed?
 *                 path information.  The structure is recursive so it is?
 *                 possible to model both an ordered list of connection segments,
 *                 as well as the hierarchical connection segments created on?
 *                 children NSA in either a tree and chain configuration.
 *                 
 *                 Attributes:
 *                 
 *                 order - Specification of ordered path elements.
 *                 
 *                 Elements:
 *                 
 *                 providerNSA - The provider NSA holding the connection
 *                 information associated with this instance of data.
 *                 
 *                 connectionId - The connection identifier associated with the
 *                 reservation and path segment.
 *                 
 *                 connectionStates - This reservation's segments connection
 *                 states.
 *                 
 *                 path - The path information associated with the connection
 *                 reservation.
 *                 
 *                 children - If provided this element will contain the list of?
 *                 connections in the context of all direct children NSA involved?
 *                 in the connection path.
 *             
 * 
 * <p>Java class for DetailedPathType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DetailedPathType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="providerNSA" type="{http://schemas.ogf.org/nsi/2012/03/framework/types}NsaIdType"/>
 *         &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ConnectionIdType"/>
 *         &lt;element name="connectionStates" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ConnectionStatesType"/>
 *         &lt;element name="path" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}PathType" minOccurs="0"/>
 *         &lt;element name="children" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ChildDetailedListType" minOccurs="0"/>
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
@XmlType(name = "DetailedPathType", propOrder = {
    "providerNSA",
    "connectionId",
    "connectionStates",
    "path",
    "children"
})
public class DetailedPathType {

    @XmlElement(required = true)
    protected String providerNSA;
    @XmlElement(required = true)
    protected String connectionId;
    @XmlElement(required = true)
    protected ConnectionStatesType connectionStates;
    protected PathType path;
    protected ChildDetailedListType children;
    @XmlAttribute(required = true)
    protected int order;

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
     * Gets the value of the children property.
     * 
     * @return
     *     possible object is
     *     {@link ChildDetailedListType }
     *     
     */
    public ChildDetailedListType getChildren() {
        return children;
    }

    /**
     * Sets the value of the children property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChildDetailedListType }
     *     
     */
    public void setChildren(ChildDetailedListType value) {
        this.children = value;
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
