package net.es.oscars.pss.stub.common;

import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.PSSRequest;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.notify.CoordNotifier;
import net.es.oscars.pss.soap.gen.ModifyReqContent;
import net.es.oscars.pss.soap.gen.PSSPortType;
import net.es.oscars.pss.soap.gen.SetupReqContent;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.pss.soap.gen.StatusReqContent;
import net.es.oscars.pss.soap.gen.TeardownReqContent;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.svc.ServiceNames;

/**
 * main entry point for PSS
 *
 * @author haniotak,mrt
 *
 */
@javax.jws.WebService(
        serviceName = ServiceNames.SVC_PSS,
        targetNamespace = "http://oscars.es.net/OSCARS/pss",
        portName = "PSSPort",
        endpointInterface = "net.es.oscars.pss.soap.gen.PSSPortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class StubPSSSoapHandler implements PSSPortType {

    private static final Logger log = Logger.getLogger(StubPSSSoapHandler.class.getName());
    private static final String moduleName = ModuleName.PSS;

    public void setup(SetupReqContent setupReq) {
        String event = "setup";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,setupReq.getTransactionId());
        String gri = setupReq.getReservation().getGlobalReservationId();
        netLogger.setGRI(gri);
        log.info(netLogger.start(event));
 
        boolean simulateFailure = false;
        if (setupReq.getTransactionId().equals("error")) {
            simulateFailure = true;
        }
        

        PSSAction act = new PSSAction();
        CoordNotifier coordNotify = new CoordNotifier();
        try {
            PSSRequest req = new PSSRequest();
            req.setSetupReq(setupReq);
            req.setRequestType(PSSRequest.PSSRequestTypes.SETUP);

            act.setRequest(req);
            act.setActionType(net.es.oscars.pss.enums.ActionType.SETUP);
            if (!simulateFailure) {
                act.setStatus(ActionStatus.SUCCESS);
            } else {
                OSCARSFaultReport faultReport = new OSCARSFaultReport ();
                faultReport.setErrorMsg("simulated PSS error");
                faultReport.setErrorType(ErrorReport.SYSTEM);
                faultReport.setErrorCode(ErrorCodes.PATH_SETUP_FAILED);
                faultReport.setModuleName("StubPSS");

                act.setFaultReport(faultReport);
                act.setStatus(ActionStatus.FAIL);
            }
            log.debug(netLogger.getMsg(event,"calling coordNotify.process"));
            coordNotify.process(act);
        } catch (PSSException e) {
            log.error(netLogger.error(event,ErrSev.MAJOR,"caught PSSException " + e.getMessage()));
        }
        log.info(netLogger.end(event));
    }

    public void teardown(TeardownReqContent teardownReq) {
        String event = "teardown";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,teardownReq.getTransactionId());
        String gri = teardownReq.getReservation().getGlobalReservationId();
        netLogger.setGRI(gri);
        log.info(netLogger.start(event));
        boolean simulateFailure = false;
        if (teardownReq.getTransactionId().equals("error")) {
            simulateFailure = true;
        }
        
        PSSAction act = new PSSAction();
        CoordNotifier coordNotify = new CoordNotifier();
        try {
            PSSRequest req = new PSSRequest();
            req.setTeardownReq(teardownReq);
            req.setRequestType(PSSRequest.PSSRequestTypes.TEARDOWN);

            act.setRequest(req);
            act.setActionType(net.es.oscars.pss.enums.ActionType.TEARDOWN);
            if (!simulateFailure) {
                act.setStatus(ActionStatus.SUCCESS);
            } else {
                OSCARSFaultReport faultReport = new OSCARSFaultReport ();
                faultReport.setErrorMsg("simulated PSS error");
                faultReport.setErrorType(ErrorReport.SYSTEM);
                faultReport.setErrorCode(ErrorCodes.PATH_TEARDOWN_FAILED);
                faultReport.setModuleName("StubPSS");
                act.setFaultReport(faultReport);
                act.setStatus(ActionStatus.FAIL);
            }

            
            coordNotify.process(act);
        } catch (PSSException e) {
            log.error(netLogger.error(event,ErrSev.MAJOR,"caught PSSException " + e.getMessage()));
        }
        log.info(netLogger.end(event));
    }


    public  void modify(ModifyReqContent modifyReq)  {
        String event = "modify";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,modifyReq.getTransactionId());
        String gri = modifyReq.getReservation().getGlobalReservationId();
        netLogger.setGRI(gri);
        log.info(netLogger.start(event));
        boolean simulateFailure = false;
        if (modifyReq.getTransactionId().equals("error")) {
            simulateFailure = true;
        }
        
        PSSAction act = new PSSAction();
        CoordNotifier coordNotify = new CoordNotifier();
        try {
            PSSRequest req = new PSSRequest();
            req.setModifyReq(modifyReq);
            req.setRequestType(PSSRequest.PSSRequestTypes.MODIFY);
            act.setRequest(req);
            act.setActionType(net.es.oscars.pss.enums.ActionType.MODIFY);
            if (!simulateFailure) {
                act.setStatus(ActionStatus.SUCCESS);
            }else{
                OSCARSFaultReport faultReport = new OSCARSFaultReport ();
                faultReport.setErrorMsg("Modify not supported");
                faultReport.setErrorType(ErrorReport.SYSTEM);
                faultReport.setErrorCode(ErrorCodes.NOT_IMPLEMENTED);
                act.setFaultReport(faultReport);
                act.setStatus(ActionStatus.FAIL);
            }
            coordNotify.process(act);
        } catch (PSSException e) {
            log.error(netLogger.error(event,ErrSev.MAJOR,"caught PSSException " + e.getMessage()));
            e.printStackTrace();
        }
        log.info(netLogger.end(event));
    }

    public void status(StatusReqContent statusReq) {
        String event = "status";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,statusReq.getTransactionId());
        String gri = statusReq.getReservation().getGlobalReservationId();
        netLogger.setGRI(gri);
        log.info(netLogger.start(event));
 
        PSSAction act = new PSSAction();
        CoordNotifier coordNotify = new CoordNotifier();
        try {
            PSSRequest req = new PSSRequest();
            req.setStatusReq(statusReq);
            req.setRequestType(PSSRequest.PSSRequestTypes.STATUS);

            act.setRequest(req);
            act.setActionType(net.es.oscars.pss.enums.ActionType.STATUS);
            OSCARSFaultReport faultReport = new OSCARSFaultReport ();
            faultReport.setErrorMsg("Status not supported");
            faultReport.setErrorType(ErrorReport.SYSTEM);
            faultReport.setErrorCode(ErrorCodes.NOT_IMPLEMENTED);
            act.setFaultReport(faultReport);
            act.setStatus(ActionStatus.FAIL);
            coordNotify.process(act);
        } catch (PSSException e) {
            log.error(netLogger.error(event,ErrSev.MAJOR,"caught PSSException " + e.getMessage()));
        }
        log.info(netLogger.end(event));
    }
}
