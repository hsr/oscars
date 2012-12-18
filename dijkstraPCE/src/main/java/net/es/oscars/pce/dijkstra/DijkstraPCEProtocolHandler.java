package net.es.oscars.pce.dijkstra;

import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.pce.SimplePCEProtocolHandler;
import net.es.oscars.utils.svc.ServiceNames;

/**
 * Handles incoming SOAP requests. Extends SimplePCEProtocolHandler and 
 * annotates it to enable NetLogger for this Dijkstra PCE.
 * 
 * @author Andy Lake <andy@es.net>
 */
@OSCARSNetLoggerize(moduleName = "net.es.oscars.pce.Dijkstra", serviceName = ServiceNames.SVC_PCE_DIJ)
@javax.xml.ws.BindingType(value ="http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class DijkstraPCEProtocolHandler extends SimplePCEProtocolHandler{}
