package net.es.oscars.api.forwarder;

import java.net.URL;

import net.es.oscars.api.soap.gen.v06.CancelResContent;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.api.soap.gen.v06.CreatePathResponseContent;
import net.es.oscars.api.soap.gen.v06.CreatePathContent;
import net.es.oscars.api.soap.gen.v06.InterDomainEventContent;
import net.es.oscars.api.soap.gen.v06.GetTopologyContent;
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

/**
 * This abstract class is to be extended by the IDC message forwarders, implementing when necessary, protocol adaptation.
 * 
 * @author lomax
 *
 */

public abstract class Forwarder {
    
    protected String destDomainId = null;
    protected URL    url          = null;
    
    public Forwarder (String destDomainId, URL url) {
        this.destDomainId = destDomainId;
        this.url = url;
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#createPath(CreatePathContent  createPath ,)String  destDomainId )*
     */
    public CreatePathResponseContent createPath(CreatePathContent createPath) throws OSCARSServiceException {
        throw new RuntimeException ("Method is not implemented");
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#notify(EventContent  notify ,)String  destDomainId )*
     */
    public void notify(InterDomainEventContent notify) throws OSCARSServiceException { 
        throw new RuntimeException ("Method is not implemented");
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#cancelReservation(GlobalReservationId  cancelReservation ,)String  destDomainId )*
     */
    public CancelResReply cancelReservation(CancelResContent cancelReservation) throws OSCARSServiceException    { 
        throw new RuntimeException ("Method is not implemented");
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#teardownPath(TeardownPathContent  teardownPath ,)String  destDomainId )*
     */
    public TeardownPathResponseContent teardownPath(TeardownPathContent teardownPath) throws OSCARSServiceException    { 
        throw new RuntimeException ("Method is not implemented");
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
        throw new RuntimeException ("Method is not implemented");
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
        throw new RuntimeException ("Method is not implemented");
    }
    
    public String getDestDomainId () {
        return this.destDomainId;
    }

    public URL getDestURL () {
        return this.url;
    }
}
