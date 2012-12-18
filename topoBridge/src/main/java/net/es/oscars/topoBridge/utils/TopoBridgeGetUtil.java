package net.es.oscars.topoBridge.utils;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.utils.clients.TopoBridgeClient;
import net.es.oscars.topoBridge.soap.gen.GetTopologyRequestType;
import net.es.oscars.topoBridge.soap.gen.GetTopologyResponseType;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;


public class TopoBridgeGetUtil {
    private TopoBridgeClient client;
    
    final static private String defaultUrl = "http://localhost:9019/topoBridge";
    
    static {
        Logger.getRootLogger().setLevel(Level.OFF);
    }
    
    public TopoBridgeGetUtil(String url) throws MalformedURLException, OSCARSServiceException{
        this.client = TopoBridgeClient.getClient(url);
    }
    
    public void getTopology(String domainName) throws OSCARSServiceException{
        GetTopologyRequestType topoRequest = new GetTopologyRequestType();
        topoRequest.getDomainId().add(domainName);
        MessagePropertiesType msgProps = new MessagePropertiesType();
        msgProps.setGlobalTransactionId(PathTools.getLocalDomainId() + "-" + UUID.randomUUID().toString());
        topoRequest.setMessageProperties(msgProps);
        Object[] request = {topoRequest};
        Object[] response =this.client.invoke("getTopology",request);
        if (response == null) {
            System.err.println("No topology found");
            System.exit(1);
        }
        GetTopologyResponseType topoResponse = (GetTopologyResponseType)response[0];
        if( topoResponse.getTopology() == null){
            System.err.println("No topology found");
            System.exit(1);
        }
        for(CtrlPlaneTopologyContent topo : topoResponse.getTopology()){
            for(CtrlPlaneDomainContent domain : topo.getDomain()){
                System.out.println("Domain ID: " + domain.getId());
                for(CtrlPlaneNodeContent node : domain.getNode()){
                    for(CtrlPlanePortContent port : node.getPort()){
                        for(CtrlPlaneLinkContent link : port.getLink()){
                            System.out.println("    " + link.getId());
                        }
                    }
                }
            }
        }
    }
    
    public static void main(String[] args){
        String helpMsg = "\nUsage: oscars-lookup [opts] <domain>\n\n";
        helpMsg += "The <domain> argument is the name " +
                "of the domain with the topology you wish to find.";
        
        String url = defaultUrl;
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("u", "url"), "the URL of the OSCARS topology bridge to contact").withRequiredArg().ofType(String.class);
            }
        };
        
        try {
            OptionSet opts = parser.parse(args);
            if(opts.has("h")){
                System.out.println(helpMsg);
                try{
                    parser.printHelpOn(System.out);
                }catch(Exception e){}
            }
            List<String> nonOpts = opts.nonOptionArguments();
            if(nonOpts.size() < 1){
                System.out.println("Please specify a domain.");
                System.exit(1);
            }
            
            if(opts.has("u")){
                url = (String)opts.valueOf("u");
            }
            
            TopoBridgeGetUtil util = new TopoBridgeGetUtil(url);
            util.getTopology(nonOpts.get(0));
        }catch(OptionException e){
            System.out.println(e.getMessage());
            System.out.println(helpMsg);
            try{
                parser.printHelpOn(System.out);
            }catch(Exception e1){}
            System.exit(1);
        }catch (MalformedURLException e) {
            System.out.println("Invalid URL provided for OSCARS lookup module");
            System.exit(1);
        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
