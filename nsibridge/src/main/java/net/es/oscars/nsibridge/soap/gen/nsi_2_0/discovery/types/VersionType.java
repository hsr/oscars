
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.discovery.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;


/**
 * 
 *                 Type definition for a service.  A service may consist of zero
 *                 or more versions.
 *                 
 *                 Elements:
 *                 capabilities - a list of capabilities supported by the NSA for
 *                 this version of the service.
 *                 
 *                 any - Provides a flexible mechanism allowing additional elements
 *                 to be provided in the discovery message.  Use of this element
 *                 field is beyond the current scope of this NSI specification, but
 *                 may be used in the future to extend the existing protocol without
 *                 requiring a schema change.  Additionally, the field can be used
 *                 between peered NSA to provide additional context not covered in
 *                 the existing specification, however, this is left up to specific
 *                 peering agreements.                
 *                 
 *                 Attributes:
 *                 name - A friendly name for the service version such as
 *                 "NSI-CS-1.SC-Provider", "NSI-CS-1.1-Provider", or
 *                 "NSI-Topology-2.0-Provider".
 *                 
 *                 version - The URL uniquely identifying the service version.
 *                 For example, the following URL identify NSI-CS-1.SC-Provider"
 *                 and "NSI-CS-1.1-Provider" respectively:
 *                 
 *                 http://schemas.ogf.org/nsi/2011/10/connection/provider
 *                 http://schemas.ogf.org/nsi/2011/12/connection/provider
 *                 
 *                 endpoint - A URL representing the SOAP endpoint on which this
 *                 version of the service is available.
 *                 
 *                 wsdl - An optional URL providing remote download of the WSDL
 *                 definition.
 *                 
 *                 anyAttribute - Provides a flexible mechanism allowing additional
 *                 attributes in the discovery message exchange between two peered
 *                 NSA.  Use of this attribute field is beyond the current scope of
 *                 this NSI specification, but may be used in the future to extend
 *                 the existing protocol without requiring a schema change.
 *                 Additionally, the field can be used between peered NSA to provide
 *                 additional context not covered in the existing specification,
 *                 however, this is left up to specific peering agreements.                
 *             
 * 
 * <p>Java class for VersionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VersionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="capabilities" type="{http://schemas.ogf.org/nsi/2012/03/discovery/types}CapabilitiesType"/>
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="endpoint" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="wsdl" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VersionType", propOrder = {
    "capabilities",
    "any"
})
public class VersionType {

    @XmlElement(required = true)
    protected CapabilitiesType capabilities;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String version;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String endpoint;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    protected String wsdl;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the capabilities property.
     * 
     * @return
     *     possible object is
     *     {@link CapabilitiesType }
     *     
     */
    public CapabilitiesType getCapabilities() {
        return capabilities;
    }

    /**
     * Sets the value of the capabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link CapabilitiesType }
     *     
     */
    public void setCapabilities(CapabilitiesType value) {
        this.capabilities = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the endpoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the value of the endpoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndpoint(String value) {
        this.endpoint = value;
    }

    /**
     * Gets the value of the wsdl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWsdl() {
        return wsdl;
    }

    /**
     * Sets the value of the wsdl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWsdl(String value) {
        this.wsdl = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
