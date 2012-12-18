package net.es.oscars.api.forwarder;

import java.net.MalformedURLException;
import java.net.URL;

import net.es.oscars.api.soap.gen.v06.CancelResContent;
import net.es.oscars.api.soap.gen.v06.CreatePathResponseContent;
import net.es.oscars.api.soap.gen.v06.CreatePathContent;
import net.es.oscars.api.soap.gen.v06.EventContent;
import net.es.oscars.api.soap.gen.v06.GetTopologyContent;
import net.es.oscars.api.soap.gen.v06.InterDomainEventContent;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ModifyResContent;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.RefreshPathContent;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.api.soap.gen.v06.TeardownPathContent;
import net.es.oscars.api.soap.gen.v06.CancelResReply;
import net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.RefreshPathResponseContent;
import net.es.oscars.api.soap.gen.v06.GetTopologyResponseContent;
import net.es.oscars.api.soap.gen.v06.ModifyResReply;


import net.es.oscars.utils.clients.IDCClient06;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.log4j.Logger;

/**
 * This abstract class is to be extended by the IDC message forwarders, implementing when necessary, protocol adaptation.
 * 
 * @author lomax
 *
 */

public class Forwarder06 extends Forwarder {
    
    private static Logger LOG = Logger.getLogger(Forwarder06.class.getName());
    private static final String DEFAULT_CONNTYPE = "x509";
    
    private String      destDomainId = null;
    private URL         url          = null;
    private IDCClient06 client       = null;
    
    public Forwarder06 (String destDomainId, URL url) throws OSCARSServiceException {
        super (destDomainId, url);
        this.url = url;
        // Instantiates Forwarder06 client
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_API);
   
        URL wsdl;
        try {
            wsdl = cc.getWSDLPath(ServiceNames.SVC_API,"0.6");
            this.client = IDCClient06.getClient(this.url, wsdl, Forwarder06.DEFAULT_CONNTYPE);
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException ("Cannot create IDC 0.6 client for " + this.url);
        }
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#createPath(CreatePathContent  createPath ,)String  destDomainId )*
     */
    public CreatePathResponseContent createPath(CreatePathContent createPath) throws OSCARSServiceException {
 
        Object[] req = {createPath};
        Object[] res = this.client.invoke("createPath",req);
 
        if ((res == null) || (res[0] == null)) {
            throw new OSCARSServiceException ("no response from IDC " + this.getDestDomainId() + " url= " + this.getDestURL(), "system");
        }
        CreatePathResponseContent response = (CreatePathResponseContent) res[0];
        
        return response; 
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#notify(EventContent  notify ,)String  destDomainId )*
     */
    public void notify(InterDomainEventContent notify) throws OSCARSServiceException {
        Object[] req = {notify};
        this.client.invoke("interDomainEvent",req);
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#cancelReservation(GlobalReservationId  cancelReservation ,)String  destDomainId )*
     */
    public CancelResReply cancelReservation(CancelResContent cancelReservation) throws OSCARSServiceException    { 
        Object[] req = {cancelReservation};
        Object[] res = this.client.invoke("cancelReservation",req);
 
        if ((res == null) || (res[0] == null)) {
            throw new OSCARSServiceException ("no response from IDC " + this.getDestDomainId() + " url= " + this.getDestURL(), "system");
        }
        CancelResReply response = (CancelResReply) res[0];
        
        return response; 
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#teardownPath(TeardownPathContent  teardownPath ,)String  destDomainId )*
     */
    public TeardownPathResponseContent teardownPath(TeardownPathContent teardownPath) throws OSCARSServiceException    { 
        Object[] req = {teardownPath};
        Object[] res = this.client.invoke("teardownPath",req);
 
        if ((res == null) || (res[0] == null)) {
            throw new OSCARSServiceException ("no response from IDC " + this.getDestDomainId() + " url= " + this.getDestURL(), "system");
        }
        TeardownPathResponseContent response = (TeardownPathResponseContent) res[0];
        
        return response; 
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#queryReservation(GlobalReservationId  queryReservation ,)String  destDomainId )*
     */
    public QueryResReply queryReservation(QueryResContent queryReservation) throws OSCARSServiceException    { 
        throw new RuntimeException ("Method is not implemented");
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#createReservation(ResCreateContent  createReservation ,)String  destDomainId )*
     */
    public CreateReply createReservation(ResCreateContent createReservation) throws OSCARSServiceException    { 
        
        Object[] req = {createReservation};
        Object[] res = this.client.invoke("createReservation",req);
 
        if ((res == null) || (res[0] == null)) {
            throw new OSCARSServiceException ("no response from IDC " + this.getDestDomainId() + " url= " + this.getDestURL(), "system");
        }
        CreateReply response = (CreateReply) res[0];
        
        return response;
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#listReservations(ListRequest  listReservations ,)String  destDomainId )*
     */
    public ListReply listReservations(ListRequest listReservations) throws OSCARSServiceException    { 
        throw new RuntimeException ("Method is not implemented");
    }
    /* (non-Javadoc)
     * @see OSCARSInternalPortType#refreshPath(RefreshPathContent  refreshPath ,)String  destDomainId )*
     */
    public RefreshPathResponseContent refreshPath(RefreshPathContent refreshPath) throws OSCARSServiceException    { 
        throw new RuntimeException ("Method is not implemented");
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#getNetworkTopology(GetTopologyContent  getNetworkTopology ,)String  destDomainId )*
     */
    public GetTopologyResponseContent getNetworkTopology(GetTopologyContent getNetworkTopology) throws OSCARSServiceException    { 
        throw new RuntimeException ("Method is not implemented");
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#modifyReservation(ModifyResContent  modifyReservation ,)String  destDomainId )*
     */
    public ModifyResReply modifyReservation(ModifyResContent modifyReservation) throws OSCARSServiceException    { 
        Object[] req = {modifyReservation};
        Object[] res = this.client.invoke("modifyReservation",req);
 
        if ((res == null) || (res[0] == null)) {
            throw new OSCARSServiceException ("no response from IDC " + this.getDestDomainId() + " url= " + this.getDestURL(), "system");
        }
        ModifyResReply response = (ModifyResReply) res[0];
        
        return response; 
    }
    
    public String getDestDomainId () {
        return this.destDomainId;
    }

    public URL getDestURL () {
        return this.url;
    }
}
