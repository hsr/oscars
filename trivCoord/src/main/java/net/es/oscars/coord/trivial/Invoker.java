package net.es.oscars.coord.trivial;

import static java.util.Arrays.asList;

import java.net.URL;

import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSSoapService;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Invoker {
    public static void main(String[] args) throws Exception {

        // create a parser
        OptionParser parser = new OptionParser() {
            {
                acceptsAll( asList( "h", "?" ), "show help then exit" );
            }
        };
    				
        OptionSet options = parser.parse( args );

        // check for help
        if ( options.has( "?" ) || options.has("h") ) {
            parser.printHelpOn( System.out );
            System.exit(0);
        }
        // Set SSL keystores
        OSCARSSoapService.setSSLBusConfiguration((
                new URL(("file:" + (new SharedConfig ("CoordService")).getFilePath("server-cxf.xml")))));  
        
        CoordSoapServer s = CoordSoapServer.getInstance();
        s.startServer(false);
    }

}
