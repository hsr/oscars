package net.es.oscars.client;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;

import net.es.oscars.api.soap.gen.v06.CancelResContent;
import net.es.oscars.api.soap.gen.v06.CancelResReply;
import net.es.oscars.api.soap.gen.v06.CreatePathContent;
import net.es.oscars.api.soap.gen.v06.CreatePathResponseContent;
import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ModifyResContent;
import net.es.oscars.api.soap.gen.v06.ModifyResReply;
import net.es.oscars.api.soap.gen.v06.OSCARS;
import net.es.oscars.api.soap.gen.v06.OSCARSService;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.api.soap.gen.v06.TeardownPathContent;
import net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;

public class OSCARSClient extends Client<OSCARS>{
    
    final private String NAMESPACE = "http://oscars.es.net/OSCARS/06";
    final private String SERVICE_NAME = "OSCARSService";
    
    public final static String STATUS_ACCEPTED = "ACCEPTED";       // createReservation is authorized, gri is assigned
    public final static String STATUS_INPATHCALCULATION = "INPATHCALCULATION";   //start local path calculation
    public final static String STATUS_PATHCALCULATED  = "PATHCALCULATED"; // whole path calculation done
    public final static String STATUS_INCOMMIT = "INCOMMIT";       // in commit phase for calculated path
    public final static String STATUS_COMMITTED = "COMMITTED";     // whole path resources committed
    public final static String STATUS_RESERVED = "RESERVED";       // all domains have committed resources
    public final static String STATUS_INSETUP = "INSETUP";         // circuit setup has been started
    public final static String STATUS_ACTIVE = "ACTIVE";           // entire circuit has been setup
    public final static String STATUS_INTEARDOWN = "INTEARDOWN";   // circuit teardown has been started
    public final static String STATUS_FINISHED = "FINISHED";       // reservation endtime reached with no errors, circuit has been torndown
    public final static String STATUS_CANCELLED = "CANCELLED";     // complete reservation has been canceled, no circuit
    public final static String STATUS_FAILED = "FAILED";           // reservation failed at some point, no circuit
    public final static String STATUS_INMODIFY = "INMODIFY";       // reservation is being modified
    public final static String STATUS_INCANCEL = "INCANCEL";       // reservation is being canceled
    public final static String STATUS_OK = "Ok";
    public final static String TOPIC_RESERVATION = "idc:RESERVATION";
    
    public OSCARSClient(String oscarsUrl, String wsdlUrl) throws OSCARSClientException{
        //Create PortType
        URL wsdlUrlObj = null;
        try {
            wsdlUrlObj = new URL(wsdlUrl);
        } catch (MalformedURLException e) {
            throw new OSCARSClientException("Malformed URL " + wsdlUrl);
        }
        this.prepareSSLForWSDL();
        OSCARSService service = new OSCARSService(wsdlUrlObj, new QName (NAMESPACE, SERVICE_NAME));
        this.portType = (OSCARS) service.getPort(OSCARS.class);
        this.setServiceEndpoint(oscarsUrl);
    }
    
    public OSCARSClient(String oscarsUrl) throws OSCARSClientException{
        this(oscarsUrl, oscarsUrl + "?wsdl");
    }
    
    public CreateReply createReservation(ResCreateContent request) throws OSCARSFaultMessage, OSCARSClientException{
        this.prepareClient();
        return this.portType.createReservation(request);
    }
    
    public CancelResReply cancelReservation(CancelResContent request) throws OSCARSFaultMessage, OSCARSClientException{
        this.prepareClient();
        return this.portType.cancelReservation(request);
    }
    
    public ModifyResReply modifyReservation(ModifyResContent request) throws OSCARSFaultMessage, OSCARSClientException{
        this.prepareClient();
        return this.portType.modifyReservation(request);
    }
    
    public ListReply listReservations(ListRequest request) throws OSCARSFaultMessage, OSCARSClientException{
        this.prepareClient();
        return this.portType.listReservations(request);
    }
    
    public QueryResReply queryReservation(QueryResContent request) throws OSCARSFaultMessage, OSCARSClientException{
        this.prepareClient();
        return this.portType.queryReservation(request);
    }
    
    public CreatePathResponseContent createPath(CreatePathContent request) throws OSCARSFaultMessage, OSCARSClientException{
        this.prepareClient();
        return this.portType.createPath(request);
    }
    
    public TeardownPathResponseContent teardownPath(TeardownPathContent request) throws OSCARSFaultMessage, OSCARSClientException{
        this.prepareClient();
        return this.portType.teardownPath(request);
    }
}
