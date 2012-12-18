package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.*;
import net.es.oscars.nsibridge.ifces.NSIMessage;
import net.es.oscars.nsibridge.prov.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.requester.ConnectionRequesterPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.requester.ConnectionServiceRequester;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import java.net.MalformedURLException;
import java.net.URL;

public class SendNSIMessageTask extends Task  {
    private static final Logger log = Logger.getLogger(SendNSIMessageTask.class);

    private String connId = "";
    private NSIMessage message;


    public SendNSIMessageTask(String connId, NSIMessage message) {
        this.scope = "nsi";
        this.connId = connId;
        this.message = message;
    }

    public void onRun() throws TaskException {

        try {
            super.onRun();
            log.debug(this.id + " starting");


            NSI_ConnectionHolder ch = NSI_ConnectionHolder.getInstance();
            NSIConnection conn = ch.findConnection(connId);
            if (conn != null) {
                log.debug("found connection entry for connId: "+connId);
            } else {
                throw new TaskException("could not find connection entry for connId: "+connId);
            }

            RequestHolder rh = RequestHolder.getInstance();
            ResvRequest rreq = rh.findResvRequest(connId);


            if (rreq != null) {
                log.debug("found request for connId: "+connId);
            } else {
                log.error("no request found, exiting");
                return;
            }

            String gri = rreq.getGlobalReservationId();


            String reqReplyTo;
            ProvRequest preq;
            TermRequest treq;
            RelRequest lreq;
            CommonHeaderType reqHd;
            switch (message) {
                case RESV_CF:
                    reqReplyTo = rreq.getInHeader().getReplyTo();
                    reqHd = rreq.getInHeader();
                    break;
                case RESV_FL:
                    reqReplyTo = rreq.getInHeader().getReplyTo();
                    reqHd = rreq.getInHeader();
                    break;
                case PROV_CF:
                    preq = rh.findProvRequest(connId);
                    reqReplyTo = preq.getInHeader().getReplyTo();
                    reqHd = preq.getInHeader();

                    break;
                case PROV_FL:
                    preq = rh.findProvRequest(connId);
                    reqReplyTo = preq.getInHeader().getReplyTo();
                    reqHd = preq.getInHeader();

                    break;
                case REL_CF:
                    lreq = rh.findRelRequest(connId);
                    reqReplyTo = lreq.getInHeader().getReplyTo();
                    reqHd = lreq.getInHeader();

                    break;

                case REL_FL:
                    lreq = rh.findRelRequest(connId);
                    reqReplyTo = lreq.getInHeader().getReplyTo();
                    reqHd = lreq.getInHeader();

                    break;
                case TERM_CF:
                    treq = rh.findTermRequest(connId);
                    reqReplyTo = treq.getInHeader().getReplyTo();
                    reqHd = treq.getInHeader();

                    break;
                case TERM_FL:
                    treq = rh.findTermRequest(connId);
                    reqReplyTo = treq.getInHeader().getReplyTo();
                    reqHd = treq.getInHeader();
                    break;
                default:
                    throw new TaskException("invalid NSI message type: "+message);
            }






            log.debug("connId: "+connId+" replyTo: "+reqReplyTo);
            URL url;
            try {
                url = new URL(reqReplyTo);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return;
            }

            ConnectionServiceRequester client = new ConnectionServiceRequester();
            ConnectionRequesterPort port = client.getConnectionServiceRequesterPort();
            BindingProvider bp = (BindingProvider) port;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url.toString());


            CommonHeaderType hd = NSI_Util.makeNsiOutgoingHeader(reqHd);



            Holder outHolder = new Holder<CommonHeaderType>();
            ServiceExceptionType st = NSI_Util.makeServiceException("internal error");
            ConnectionStatesType cst = NSI_Util.makeConnectionStates(connId);

            switch (message) {
                case RESV_CF:
                    ReserveConfirmedType rct = new ReserveConfirmedType();
                    rct.setConnectionId(connId);
                    rct.setDescription(rreq.getDescription());
                    ReservationRequestCriteriaType rrct = rreq.getCriteria();
                    ReservationConfirmCriteriaType rcfct = new ReservationConfirmCriteriaType();
                    rcfct.setBandwidth(rrct.getBandwidth());
                    rcfct.setPath(rrct.getPath());
                    rcfct.setSchedule(rrct.getSchedule());
                    rcfct.setServiceAttributes(rrct.getServiceAttributes());
                    rcfct.setVersion(1);
                    rct.getCriteria().add(rcfct);
                    rct.setGlobalReservationId(gri);
                    Holder ho = NSI_Util.makeHolder(hd);
                    port.reserveConfirmed(rct, ho);
                    break;
                case RESV_FL:
                    port.reserveFailed(     gri, connId, cst, st, hd, outHolder);
                    break;
                case PROV_CF:
                    port.provisionConfirmed(gri, connId, hd, outHolder);
                    break;
                case PROV_FL:
                    port.provisionFailed(   gri, connId, cst, st, hd, outHolder);
                    break;
                case REL_CF:
                    port.releaseConfirmed(  gri, connId, hd, outHolder);
                    break;
                case REL_FL:
                    port.releaseFailed(     gri, connId, cst, st, hd, outHolder);
                    break;
                case TERM_CF:
                    port.terminateConfirmed(gri, connId, hd, outHolder);
                    break;
                case TERM_FL:
                    port.terminateFailed(   gri, connId, cst, st, hd, outHolder);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex);
            this.onFail();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }



}
