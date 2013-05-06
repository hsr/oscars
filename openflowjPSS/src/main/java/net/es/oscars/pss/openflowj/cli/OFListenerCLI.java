package net.es.oscars.pss.openflowj.cli;

import java.io.IOException;
import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import net.es.oscars.pss.openflowj.io.OpenFlowListener;
import net.es.oscars.pss.openflowj.io.OpenFlowServerMessageHandler;

public class OFListenerCLI {
	public static void main(String[] args) throws IOException{
		OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("p", "port"), "the port to listen for OpenFlow connections. Defaults to " + OpenFlowListener.DEFAULT_PORT).withRequiredArg().ofType(Integer.class);
            }
        };
        
        OptionSet opts = parser.parse(args);
        
        if(opts.has("h")){
            parser.printHelpOn(System.out);
            System.exit(0);
        }
        
        int port = OpenFlowListener.DEFAULT_PORT;
        if(opts.has("p")){
        	port = (Integer)opts.valueOf("p");
        }
        
		OpenFlowListener listener = new OpenFlowListener(port, new OpenFlowServerMessageHandler());
		listener.start();
	}
}
