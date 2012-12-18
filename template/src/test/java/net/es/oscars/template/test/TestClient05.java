
package net.es.oscars.template.test;

import java.net.URL;

import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;

import net.es.oscars.template.soap.gen.v05.RequestType;
import net.es.oscars.template.soap.gen.v05.TemplatePortType;
import net.es.oscars.template.soap.gen.v05.TemplateService;

@OSCARSService (
		implementor = "net.es.oscars.template.soap.gen.v05.TemplateService",
		namespace = "http://oscars.es.net/OSCARS/template/05",
		serviceName = "TemplateService",
		config="config.yaml"
)
public final class TestClient05 extends OSCARSSoapService<TemplateService,TemplatePortType> {

    public TestClient05(URL host, URL wsdlFile,URL keyStoreConf) throws OSCARSServiceException {
    	super (host, wsdlFile, TemplatePortType.class);
    }
 
    public static void main(String args[]) {

    	try {

            OSCARSSoapService.setSSLBusConfiguration((
                    new URL("file:" + (new SharedConfig ("TemplateService")).getFilePath("client-cxf.xml"))));
            
        	TestClient05 client = new TestClient05 (new URL(args[0]), new URL (args[1]), new URL(args[2]));
     
            RequestType request = new RequestType();
            request.setPrefix("PrefixInput");
 
    		Object[] req = new Object[]{request};
    		Object[] res = client.invoke("Query",req);
 
    		System.out.println("Response: " + (String) res[0]);
    		
    	} catch (Exception ee) {
    		System.out.println ("Exception" + ee);
    	}

        System.exit(0);
    }

}

