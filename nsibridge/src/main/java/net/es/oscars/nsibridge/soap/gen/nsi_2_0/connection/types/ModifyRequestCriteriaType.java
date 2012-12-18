
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.TypeValuePairListType;


/**
 * 
 *                 Type definition for a reservation modification request criteria.
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
 * 
 * <p>Java class for ModifyRequestCriteriaType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ModifyRequestCriteriaType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="schedule" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ScheduleType" minOccurs="0"/>
 *         &lt;element name="bandwidth" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="serviceAttributes" type="{http://schemas.ogf.org/nsi/2012/03/framework/types}TypeValuePairListType" minOccurs="0"/>
 *         &lt;element name="path" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}PathType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModifyRequestCriteriaType", propOrder = {
    "schedule",
    "bandwidth",
    "serviceAttributes",
    "path"
})
public class ModifyRequestCriteriaType {

    protected ScheduleType schedule;
    protected int bandwidth;
    protected TypeValuePairListType serviceAttributes;
    protected PathType path;

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

}
