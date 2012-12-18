
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import net.es.oscars.nsibridge.soap.gen.saml.assertion.AttributeStatementType;
import org.w3c.dom.Element;


/**
 * 
 *                 Type definition for the common NSI message header that is sent
 *                 as part of every NSI message exchange.
 *                                 
 *                 Mandatory Elements:
 *                 
 *                 protocolVersion - A URI identifying the specific protocol
 *                 version carried in this NSI message.  The protocol version is
 *                 modeled separately from the namespace of the WSDL and XML
 *                 schema to capture behavioral changes that cannot be modeled in
 *                 schema definition, and to avoid updating of the schema
 *                 namespace.
 *                 
 *                 correlationId - An identifier provided by the requesting NSA
 *                 used to correlate to an asynchronous response from the
 *                 responder. It is recommended that a Universally Unique
 *                 Identifier (UUID) URN as per IETF RFC 4122 be used as a
 *                 globally unique value.
 *                 
 *                 requesterNSA - The NSA identifier for the NSA acting in the
 *                 Requester Agent role for the specific NSI operation.
 *                 
 *                 providerNSA - The NSA identifier for the NSA acting in the
 *                 Provider Agent role for the specific NSI operation.
 *                 
 *                 Optional Elements:
 *                 
 *                 replyTo - The Requester NSA's SOAP endpoint address to which
 *                 asynchronous messages associated with this operation request
 *                 will be delivered.  This is only populated for the original
 *                 operation request (reserve, provision, release, terminate,
 *                 and query), and not for any additional messaging associated
 *                 with the operation.  If no endpoint value is provided in a
 *                 operation request, then it is assumed the requester is not
 *                 interested in a response and will use alternative mechanism to
 *                 determine the result.
 *                 
 *                 sessionSecurityAttr - Security attributes associated with the
 *                 end user's NSI session.  This field can be used to perform
 *                 authentication, authorization, and policy enforcement of end
 *                 user requests.  Is only provided in the operation request
 *                 (reserve, provision, release, terminate, and query), and not
 *                 for any additional messaging associated with the operation.
 *                 
 *                 any - Provides a flexible mechanism allowing additional
 *                 elements in the protocol header for exchange between two
 *                 peered NSA.  Use of this element field is beyond the current
 *                 scope of this NSI specification, but may be used in the future
 *                 to extend the existing protocol without requiring a schema
 *                 change.  Additionally, the field can be used between peered
 *                 NSA to provide additional context not covered in the existing
 *                 specification, however, this is left up to specific peering
 *                 agreements.                
 * 
 *                 Optional Attributes:
 * 
 *                 anyAttribute - Provides a flexible mechanism allowing
 *                 additional attributes in the protocol header for exchange
 *                 between two peered NSA.  Use of this attribute field is beyond
 *                 the current scope of this NSI specification, but may be used
 *                 in the future to extend the existing protocol without
 *                 requiring a schema change.  Additionally, the field can be
 *                 used between peered NSA to provide additional context not
 *                 covered in the existing specification, however, this is left
 *                 up to specific peering agreements.
 *             
 * 
 * <p>Java class for CommonHeaderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CommonHeaderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="protocolVersion" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="correlationId" type="{http://schemas.ogf.org/nsi/2012/03/framework/types}UuidType"/>
 *         &lt;element name="requesterNSA" type="{http://schemas.ogf.org/nsi/2012/03/framework/types}NsaIdType"/>
 *         &lt;element name="providerNSA" type="{http://schemas.ogf.org/nsi/2012/03/framework/types}NsaIdType"/>
 *         &lt;element name="replyTo" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="sessionSecurityAttr" type="{urn:oasis:names:tc:SAML:2.0:assertion}AttributeStatementType" minOccurs="0"/>
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CommonHeaderType", propOrder = {
    "protocolVersion",
    "correlationId",
    "requesterNSA",
    "providerNSA",
    "replyTo",
    "sessionSecurityAttr",
    "any"
})
public class CommonHeaderType {

    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String protocolVersion;
    @XmlElement(required = true)
    protected String correlationId;
    @XmlElement(required = true)
    protected String requesterNSA;
    @XmlElement(required = true)
    protected String providerNSA;
    @XmlSchemaType(name = "anyURI")
    protected String replyTo;
    protected AttributeStatementType sessionSecurityAttr;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the protocolVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Sets the value of the protocolVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProtocolVersion(String value) {
        this.protocolVersion = value;
    }

    /**
     * Gets the value of the correlationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets the value of the correlationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorrelationId(String value) {
        this.correlationId = value;
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
     * Gets the value of the replyTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * Sets the value of the replyTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplyTo(String value) {
        this.replyTo = value;
    }

    /**
     * Gets the value of the sessionSecurityAttr property.
     * 
     * @return
     *     possible object is
     *     {@link AttributeStatementType }
     *     
     */
    public AttributeStatementType getSessionSecurityAttr() {
        return sessionSecurityAttr;
    }

    /**
     * Sets the value of the sessionSecurityAttr property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributeStatementType }
     *     
     */
    public void setSessionSecurityAttr(AttributeStatementType value) {
        this.sessionSecurityAttr = value;
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
