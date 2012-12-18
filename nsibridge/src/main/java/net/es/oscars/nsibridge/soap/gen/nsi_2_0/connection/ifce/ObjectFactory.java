
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ServiceException_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/interface", "serviceException");
    private final static QName _Acknowledgment_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/interface", "acknowledgment");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GenericAcknowledgmentType }
     * 
     */
    public GenericAcknowledgmentType createGenericAcknowledgmentType() {
        return new GenericAcknowledgmentType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/interface", name = "serviceException")
    public JAXBElement<ServiceExceptionType> createServiceException(ServiceExceptionType value) {
        return new JAXBElement<ServiceExceptionType>(_ServiceException_QNAME, ServiceExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericAcknowledgmentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/interface", name = "acknowledgment")
    public JAXBElement<GenericAcknowledgmentType> createAcknowledgment(GenericAcknowledgmentType value) {
        return new JAXBElement<GenericAcknowledgmentType>(_Acknowledgment_QNAME, GenericAcknowledgmentType.class, null, value);
    }

}
