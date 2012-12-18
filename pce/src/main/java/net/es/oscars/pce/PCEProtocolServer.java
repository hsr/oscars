package net.es.oscars.pce;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.mortbay.log.Log;

import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.clients.PCERuntimeClient;
import net.es.oscars.utils.config.ContextConfig;

import net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType;
import net.es.oscars.pce.soap.gen.v06.PCEPortType;
import net.es.oscars.pce.soap.gen.v06.PCEService;


public class PCEProtocolServer extends OSCARSSoapService  <PCEService, PCEPortType> {
    public static URL WSDL_URL = null;
    public static final String WSDL_FILENAME = "pce-0.6.wsdl";;
    
    public PCEProtocolServer(String module) throws OSCARSServiceException {
        super(module);
    }
    
    
    public static URL getWsdlURL() throws MalformedURLException {
        return WSDL_URL;
    }  
}
