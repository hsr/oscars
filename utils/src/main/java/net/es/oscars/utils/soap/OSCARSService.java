package net.es.oscars.utils.soap;

import java.lang.annotation.*;

/**
 * @author lomax
 * The annotation interface is used to set the protocol version of the soap service.
 * 
 * version: string containing the version of the protocol (i.e. 0.5). This string will be used part
 *          of the URL of the service if it is not the most recent version (see defaultVersion).
 *          For instance, if "https://xx.xx.xx:nnnn/service/api?..." refers to the the default URL of the
 *          service, "https://xx.xx.xx:nnnn/service/0.5/api?..." refers to the 0.5 version of the service. 
 * namespace: string containing the namespace URL (i.e. http://oscars.es.net/OSCARS/template)
 * defaultVersion: set to true if the service is the current deployed version. Set to false if the 
 *          described service implements an older version of the protocol.
 * serviceName: string containing the name of the service as specified in its WSDL
 * implementor: is a string containing the full class name of the protocol implementation. In the case of
 *          server side, the implementation is the SOAP message handler (PortType). In the case of the client side, the
 *          implementation is a javax.xml.ws.Service.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface OSCARSService {
    String namespace() default "";
    String serviceName();
    String implementor();
    String config() default "config.yaml";
}
