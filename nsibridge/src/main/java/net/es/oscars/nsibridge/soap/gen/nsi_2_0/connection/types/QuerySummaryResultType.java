
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *             Type containing the set of reservation parameters associated with
 *             a "Summary" query result.
 * 
 *             Elements:
 * 
 *             ReservationInfoGroup - Element group containing the common
 *             reservation elements and detailed path data.
 *             
 *             requesterNSA - The requester NSA associated with the reservation.
 * 
 *             connectionStates - The reservation's overall connection states.
 * 
 *             children - If this connection reservation is aggregating child
 *             connections then this element contains basic summary information
 *             about the child connection segment.
 *          
 * 
 * <p>Java class for QuerySummaryResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QuerySummaryResultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.ogf.org/nsi/2012/03/connection/types}ReservationInfoGroup"/>
 *         &lt;element name="requesterNSA" type="{http://schemas.ogf.org/nsi/2012/03/framework/types}NsaIdType"/>
 *         &lt;element name="connectionStates" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ConnectionStatesType"/>
 *         &lt;element name="children" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ChildSummaryListType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuerySummaryResultType", propOrder = {
    "globalReservationId",
    "description",
    "connectionId",
    "criteria",
    "requesterNSA",
    "connectionStates",
    "children"
})
public class QuerySummaryResultType {

    protected String globalReservationId;
    protected String description;
    @XmlElement(required = true)
    protected String connectionId;
    @XmlElement(required = true)
    protected List<ReservationConfirmCriteriaType> criteria;
    @XmlElement(required = true)
    protected String requesterNSA;
    @XmlElement(required = true)
    protected ConnectionStatesType connectionStates;
    protected ChildSummaryListType children;

    /**
     * Gets the value of the globalReservationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGlobalReservationId() {
        return globalReservationId;
    }

    /**
     * Sets the value of the globalReservationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlobalReservationId(String value) {
        this.globalReservationId = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
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
     * {@link ReservationConfirmCriteriaType }
     * 
     * 
     */
    public List<ReservationConfirmCriteriaType> getCriteria() {
        if (criteria == null) {
            criteria = new ArrayList<ReservationConfirmCriteriaType>();
        }
        return this.criteria;
    }

    /**
     * Gets the value of the requesterNSA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequesterNSA() {
        return requesterNSA;
    }

    /**
     * Sets the value of the requesterNSA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequesterNSA(String value) {
        this.requesterNSA = value;
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
     * Gets the value of the children property.
     * 
     * @return
     *     possible object is
     *     {@link ChildSummaryListType }
     *     
     */
    public ChildSummaryListType getChildren() {
        return children;
    }

    /**
     * Sets the value of the children property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChildSummaryListType }
     *     
     */
    public void setChildren(ChildSummaryListType value) {
        this.children = value;
    }

}
