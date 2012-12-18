package net.es.oscars.utils.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.es.oscars.logging.OSCARSNetLogger;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import edu.internet2.perfsonar.PSException;
import edu.internet2.perfsonar.PSLookupClient;
import edu.internet2.perfsonar.dcn.DCNLookupClient;

/**
 * Utility class that has functions for converting endpoints to URNs. Currenlty
 * can convert from perfSONAR "friendly names" to NMWG urns.
 * 
 * @author alake
 *
 */
public class EndpointValidator {
    private static DCNLookupClient psLookupClient = null;
    
    public static void init(String hintsFile, 
            List<String> globalLookupServices, List<String> homeLookupServices) throws HttpException, IOException {

        //no perfsonar client used so rely on database
        if(hintsFile == null && globalLookupServices == null && homeLookupServices == null){
            return;
        }
        
        boolean useGlobals = false;
        ArrayList<String> globalList = new ArrayList<String>();
        
        //get URLs from hints file
        if(hintsFile != null){
            useGlobals = true;
            for(String url : PSLookupClient.getGlobalHints(hintsFile)){
                globalList.add(url);
            }
        }
        
        //get manual URLs
        if(globalLookupServices != null){
            useGlobals = true;
            globalList.addAll(globalLookupServices);
        }
        
        //convert to array
        String[] hlsArray = new String[0];
        if(homeLookupServices != null){
            hlsArray = homeLookupServices.toArray(
                    new String[homeLookupServices.size()]);
        }
        String[] glsArray = globalList.toArray(new String[globalList.size()]);
        
        //Create DCN client
        psLookupClient = new DCNLookupClient(glsArray, hlsArray);
        psLookupClient.setUseGlobalLS(useGlobals);
    }
    
    public static String lookupHostUrn(String hostname){
        if(psLookupClient == null){
            throw new RuntimeException("Unable to lookup " + hostname + " because no perfSONAR lookup service configured");
        }
        
        //get urn
        String urn = null;
        try {
            urn = psLookupClient.lookupHost(hostname);
        } catch (PSException e) {
            throw new RuntimeException("Unable to find " + hostname + " in lookup service: " + e.getMessage());
        }
        
        //verify its a valid urn
        if(urn == null){
            throw new RuntimeException("No URN returned by lookup service.");
        }else if(!urn.startsWith("urn:ogf:network")){
            throw new RuntimeException("Invalid urn " + urn + " returned by lookup service for " + hostname);
        }
        return urn;
    }

}
