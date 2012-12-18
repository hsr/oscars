package net.es.oscars.template.http;

import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;

import net.es.oscars.template.soap.gen.v06.TemplateService;
import net.es.oscars.template.soap.gen.v06.TemplatePortType;

@OSCARSService (
		serviceName = "TemplateService",
		implementor = "net.es.oscars.template.http.TemplateSoapHandler05"
)
public class TemplateSoapServer05 extends OSCARSSoapService <TemplateService, TemplatePortType> {

	private static TemplateSoapServer05 instance;
	    
	public static TemplateSoapServer05 getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new TemplateSoapServer05();
        }
        return instance;
    }
	     
    private TemplateSoapServer05() throws OSCARSServiceException {
        super();
    }
}
