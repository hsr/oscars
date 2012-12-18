package net.es.oscars.template.common;

import static java.util.Arrays.asList;
import java.net.URL;

import java.util.logging.Logger;

import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSSoapService;

import net.es.oscars.template.soap.gen.v06.*;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Invoker {

	static {
		(new SharedConfig ("TemplateService")).setLog4j();
	}

	private static final Logger LOG = Logger.getLogger(Invoker.class.getName());
	
    public static void main(String[] args) throws Exception {
    	
    	LOG.info("TemplateService starts.");
    	
        // create a parser
        OptionParser parser = new OptionParser() {
            {
                acceptsAll( asList( "h", "?" ), "show help then exit" );
                accepts( "help", "show extended help then exit" );
                accepts( "mode" , "server / client mode" ).withRequiredArg().describedAs("client / server (default)").ofType(String.class);
            }
        };

        OptionSet options = parser.parse( args );

        // check for help
        if ( options.has( "?" ) ) {
            parser.printHelpOn( System.out );
            System.exit(0);
        }
        
        String mode = "server";
        if (options.has("mode")) {
            String optVal = (String) options.valueOf("mode");
            if (optVal.equals("client")) {
            	mode = "client";
            } else if (!optVal.equals("server")) {
                parser.printHelpOn( System.out );
                System.exit(1);
            }
        }
        
        if (mode.equals("server")) {
            // Set the CXF bus for the server side.This configures the SSL keystores
            OSCARSSoapService.setSSLBusConfiguration((
                    new URL(("file:" + (new SharedConfig ("OSCARSService")).getFilePath("server-cxf.xml")))));
        	
        } else {
            // Set the CXF bus for the client side.This configures the SSL keystores
            OSCARSSoapService.setSSLBusConfiguration((
                    new URL(("file:" + (new SharedConfig ("OSCARSService")).getFilePath("client-cxf.xml")))));
            
            TemplateService ss = new TemplateService();
            TemplatePortType port = ss.getTemplatePort();  
            
            System.out.println("Invoking query...");
            RequestType _query_request = new RequestType ();
            _query_request.setPrefix("PREFIX");
            _query_request.setInputData("VALUE");
            java.lang.String _query__return = port.query(_query_request);
            System.out.println("query.result=" + _query__return);

        }


    }


}
