
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Type definition for the query recursive result containing
 *                 versioned reservation information and associated child
 *                 connection identifiers.
 *                 
 *                 Elements:
 *                 
 *                 schedule - Time parameters specifying the life of the service.
 *                 
 *                 serviceType - The specific service type of this reservation. 
 *                 This service type string maps into the list of supported
 *                 service descriptions defined by the network providers, and in
 *                 turn, to the specific service elements carried in the
 *                 serviceAttributes element required to specify the requested
 *                 service.
 *                 
 *                 bandwidth - Bandwidth of the service in Mb/s.
 *                 
 *                 serviceAttributes - Technology specific attributes relating to
 *                 the service.
 *                 
 *                 path - The source and destination end points of the service.
 *                 Can optionally provide additional path segments to guide path
 *                 computation.
 *                 
 *                 children - If this connection reservation is aggregating child
 *                 connections then this element contains detailed information
 *                 about the child connection segment.  The level of detail
 *                 include is left up to the individual NSA and their
 *                 authorization policies.
 *                 
 *                 Attributes:
 *                 
 *                 version - Version of the reservation instance.
 *             
 * 
 * <p>Java class for QueryRecursiveResultCriteriaType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryRecursiveResultCriteriaType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="schedule" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}ScheduleType"/>
 *         &lt;element name="serviceType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bandwidth" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="serviceAttributes" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}ServiceAttributesType" minOccurs="0"/>
 *         &lt;element name="path" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}PathType"/>
 *         &lt;element name="children" type="{http://schemas.ogf.org/nsi/2013/04/connection/types}ChildRecursiveListType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryRecursiveResultCriteriaType", propOrder = {
    "schedule",
    "serviceType",
    "bandwidth",
    "serviceAttributes",
    "path",
    "children"
})
public class QueryRecursiveResultCriteriaType {

    @XmlElement(required = true)
    protected ScheduleType schedule;
    protected String serviceType;
    protected int bandwidth;
    protected ServiceAttributesType serviceAttributes;
    @XmlElement(required = true)
    protected PathType path;
    protected ChildRecursiveListType children;
    @XmlAttribute(required = true)
    protected int version;

    /**
     * Gets the value of the schedule property.
     * 
     * @return
     *     possible object is
     *     {@link ScheduleType }
     *     
     */
    public ScheduleType getSchedule() {
        return schedule;
    }

    /**
     * Sets the value of the schedule property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScheduleType }
     *     
     */
    public void setSchedule(ScheduleType value) {
        this.schedule = value;
    }

    /**
     * Gets the value of the serviceType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * Sets the value of the serviceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceType(String value) {
        this.serviceType = value;
    }

    /**
     * Gets the value of the bandwidth property.
     * 
     */
    public int getBandwidth() {
        return bandwidth;
    }

    /**
     * Sets the value of the bandwidth property.
     * 
     */
    public void setBandwidth(int value) {
        this.bandwidth = value;
    }

    /**
     * Gets the value of the serviceAttributes property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceAttributesType }
     *     
     */
    public ServiceAttributesType getServiceAttributes() {
        return serviceAttributes;
    }

    /**
     * Sets the value of the serviceAttributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceAttributesType }
     *     
     */
    public void setServiceAttributes(ServiceAttributesType value) {
        this.serviceAttributes = value;
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
     *     {@link ChildRecursiveListType }
     *     
     */
    public ChildRecursiveListType getChildren() {
        return children;
    }

    /**
     * Sets the value of the children property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChildRecursiveListType }
     *     
     */
    public void setChildren(ChildRecursiveListType value) {
        this.children = value;
    }

    /**
     * Gets the value of the version property.
     * 
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     */
    public void setVersion(int value) {
        this.version = value;
    }

}
