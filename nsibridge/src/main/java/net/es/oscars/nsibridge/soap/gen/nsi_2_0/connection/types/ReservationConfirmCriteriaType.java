
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.TypeValuePairListType;


/**
 * 
 *                 Type definition for reservation confirmation information.
 *                 Includes the reservation version id.
 *                 
 *                 Elements:
 *                 
 *                 schedule - time parameters specifying the life of the service.
 *                 
 *                 bandwidth - bandwidth of the service in Mb/s.
 *                 
 *                 serviceAttributes - Technology specific attributes relating to
 *                 the service.
 *                 
 *                 path - The source and destination end points of the service.
 *                 Can optionally provide additional path segments to guide path
 *                 computation.
 *                 
 *                 Attributes:
 *                 
 *                 version - version of the reservation instance.
 *             
 * 
 * <p>Java class for ReservationConfirmCriteriaType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReservationConfirmCriteriaType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="schedule" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ScheduleType"/>
 *         &lt;element name="bandwidth" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="serviceAttributes" type="{http://schemas.ogf.org/nsi/2012/03/framework/types}TypeValuePairListType"/>
 *         &lt;element name="path" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}PathType"/>
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
@XmlType(name = "ReservationConfirmCriteriaType", propOrder = {
    "schedule",
    "bandwidth",
    "serviceAttributes",
    "path"
})
public class ReservationConfirmCriteriaType {

    @XmlElement(required = true)
    protected ScheduleType schedule;
    protected int bandwidth;
    @XmlElement(required = true)
    protected TypeValuePairListType serviceAttributes;
    @XmlElement(required = true)
    protected PathType path;
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
     *     {@link TypeValuePairListType }
     *     
     */
    public TypeValuePairListType getServiceAttributes() {
        return serviceAttributes;
    }

    /**
     * Sets the value of the serviceAttributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeValuePairListType }
     *     
     */
    public void setServiceAttributes(TypeValuePairListType value) {
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
