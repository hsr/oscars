
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.discovery.types;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.es.oscars.nsibridge.soap.gen.nsi_2_0.discovery.types package. 
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

    private final static QName _Services_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/discovery/types", "services");
    private final static QName _ServicesQuery_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/discovery/types", "servicesQuery");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.es.oscars.nsibridge.soap.gen.nsi_2_0.discovery.types
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ServicesQueryType }
     * 
     */
    public ServicesQueryType createServicesQueryType() {
        return new ServicesQueryType();
    }

    /**
     * Create an instance of {@link VersionsType }
     * 
     */
    public VersionsType createVersionsType() {
        return new VersionsType();
    }

    /**
     * Create an instance of {@link CapabilitiesType }
     * 
     */
    public CapabilitiesType createCapabilitiesType() {
        return new CapabilitiesType();
    }

    /**
     * Create an instance of {@link ServiceType }
     * 
     */
    public ServiceType createServiceType() {
        return new ServiceType();
    }

    /**
     * Create an instance of {@link ServicesType }
     * 
     */
    public ServicesType createServicesType() {
        return new ServicesType();
    }

    /**
     * Create an instance of {@link VersionType }
     * 
     */
    public VersionType createVersionType() {
        return new VersionType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServicesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/discovery/types", name = "services")
    public JAXBElement<ServicesType> createServices(ServicesType value) {
        return new JAXBElement<ServicesType>(_Services_QNAME, ServicesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServicesQueryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/discovery/types", name = "servicesQuery")
    public JAXBElement<ServicesQueryType> createServicesQuery(ServicesQueryType value) {
        return new JAXBElement<ServicesQueryType>(_ServicesQuery_QNAME, ServicesQueryType.class, null, value);
    }

}
