
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers package. 
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

    private final static QName _NsiHeader_QNAME = new QName("http://schemas.ogf.org/nsi/2013/07/framework/headers", "nsiHeader");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CommonHeaderType }
     * 
     */
    public CommonHeaderType createCommonHeaderType() {
        return new CommonHeaderType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CommonHeaderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", name = "nsiHeader")
    public JAXBElement<CommonHeaderType> createNsiHeader(CommonHeaderType value) {
        return new JAXBElement<CommonHeaderType>(_NsiHeader_QNAME, CommonHeaderType.class, null, value);
    }

}
