package net.es.oscars.template.http;

import java.util.logging.Logger;
import net.es.oscars.template.soap.gen.v06.*;
@javax.jws.WebService(
        serviceName = "TemplateService",
        portName = "TemplatePort",
        targetNamespace = "http://oscars.es.net/OSCARS/template/06",
        endpointInterface = "net.es.oscars.template.soap.gen.v06.TemplatePortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class TemplateSoapHandler implements TemplatePortType {

    // Implements requests
    public String query (RequestType request) {
    	return (request.getPrefix() + "*" + request.getInputData());
    }   
}
