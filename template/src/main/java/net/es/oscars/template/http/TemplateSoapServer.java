package net.es.oscars.template.http;

import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;

import net.es.oscars.template.soap.gen.v06.TemplateService;
import net.es.oscars.template.soap.gen.v06.TemplatePortType;

@OSCARSService (
		serviceName = "TemplateService",
		implementor = "net.es.oscars.template.http.TemplateSoapHandler"
)
public class TemplateSoapServer extends OSCARSSoapService <TemplateService,TemplatePortType> {

	private static TemplateSoapServer instance;
	    
	public static TemplateSoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new TemplateSoapServer();
        }
        return instance;
    }
	
    private TemplateSoapServer() throws OSCARSServiceException{
        super();
    }
}
