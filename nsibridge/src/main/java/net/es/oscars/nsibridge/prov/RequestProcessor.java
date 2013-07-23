package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.beans.*;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_Event;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.state.term.NSI_Term_Event;
import net.es.oscars.nsibridge.state.term.NSI_Term_SM;
import net.es.oscars.nsibridge.task.QueryTask;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;

import java.util.Date;

public class RequestProcessor {
    private static final Logger log = Logger.getLogger(RequestProcessor.class);
    private static RequestProcessor instance;
    private RequestProcessor() {}
    public static RequestProcessor getInstance() {
        if (instance == null) instance = new RequestProcessor();
        return instance;
    }

    public void startReserve(ResvRequest request) throws ServiceException, TaskException {
        String connId = request.getConnectionId();

        NSI_ConnectionHolder ch = NSI_ConnectionHolder.getInstance();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        RequestHolder rh = RequestHolder.getInstance();

        rh.getResvRequests().add(request);

        NSIConnection conn = ch.findConnection(request.getConnectionId());
        if (conn != null) {
            throw new ServiceException("internal error: found existing connection for new reservation with connectionId: "+connId);
        }

        smh.makeStateMachines(connId);

        conn = new NSIConnection();
        conn.setConnectionId(connId);
        ch.getConnections().add(conn);

        try {
            NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);
            rsm.process(NSI_Resv_Event.RECEIVED_NSI_RESV_RQ);
        } catch (StateException ex) {
            log.error(ex);
            throw new ServiceException("resv state machine does not allow transition: "+connId);
        }

        CommonHeaderType inHeader = request.getInHeader();
        CommonHeaderType outHeader = this.makeOutHeader(inHeader);
        request.setOutHeader(outHeader);

    }

    public void startProvision(ProvRequest request) throws ServiceException, TaskException  {
        String connId = request.getConnectionId();

        NSI_ConnectionHolder ch = NSI_ConnectionHolder.getInstance();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        RequestHolder rh = RequestHolder.getInstance();
        rh.getProvRequests().add(request);
        NSIConnection conn = ch.findConnection(request.getConnectionId());
        if (conn == null) {
            throw new ServiceException("internal error: could not find existing connection for new reservation with connectionId: "+connId);
        }
        try {
            NSI_Prov_SM sm = smh.getProvStateMachines().get(connId);
            sm.process(NSI_Prov_Event.RECEIVED_NSI_PROV_RQ);
        } catch (StateException ex) {
            log.error(ex);
            throw new ServiceException("prov state machine does not allow transition: "+connId);
        }

        CommonHeaderType inHeader = request.getInHeader();
        CommonHeaderType outHeader = this.makeOutHeader(inHeader);
        request.setOutHeader(outHeader);
    }
    public void startRelease(RelRequest request) throws ServiceException, TaskException  {
        String connId = request.getConnectionId();

        NSI_ConnectionHolder ch = NSI_ConnectionHolder.getInstance();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        RequestHolder rh = RequestHolder.getInstance();
        rh.getRelRequests().add(request);
        NSIConnection conn = ch.findConnection(request.getConnectionId());
        if (conn == null) {
            throw new ServiceException("internal error: could not find existing connection for new reservation with connectionId: "+connId);
        }
        try {
            NSI_Prov_SM sm = smh.getProvStateMachines().get(connId);
            sm.process(NSI_Prov_Event.RECEIVED_NSI_REL_RQ);
        } catch (StateException ex) {
            log.error(ex);
            throw new ServiceException("prov state machine does not allow transition: "+connId);
        }

        CommonHeaderType inHeader = request.getInHeader();
        CommonHeaderType outHeader = this.makeOutHeader(inHeader);
        request.setOutHeader(outHeader);
    }

    public void startTerminate(TermRequest request) throws ServiceException, TaskException   {
        String connId = request.getConnectionId();

        NSI_ConnectionHolder ch = NSI_ConnectionHolder.getInstance();
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        RequestHolder rh = RequestHolder.getInstance();
        rh.getTermRequests().add(request);
        NSIConnection conn = ch.findConnection(request.getConnectionId());
        if (conn == null) {
            throw new ServiceException("internal error: could not find existing connection for new reservation with connectionId: "+connId);
        }
        try {
            NSI_Term_SM sm = smh.getTermStateMachines().get(connId);
            sm.process(NSI_Term_Event.RECEIVED_NSI_TERM_RQ);
        } catch (StateException ex) {
            log.error(ex);
            throw new ServiceException("term state machine does not allow transition: "+connId);
        }

        CommonHeaderType inHeader = request.getInHeader();
        CommonHeaderType outHeader = this.makeOutHeader(inHeader);
        request.setOutHeader(outHeader);
    }


    public void startQuery(QueryRequest request) throws ServiceException, TaskException {
        RequestHolder rh = RequestHolder.getInstance();
        rh.getQueryRequests().add(request);


        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        QueryTask queryTask = new QueryTask(request);

        try {
            wf.schedule(queryTask , now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }


        CommonHeaderType inHeader = request.getInHeader();
        CommonHeaderType outHeader = this.makeOutHeader(inHeader);
        request.setOutHeader(outHeader);
    }

    private CommonHeaderType makeOutHeader(CommonHeaderType inHeader) {
        CommonHeaderType outHeader = new CommonHeaderType();
        outHeader.setCorrelationId(inHeader.getCorrelationId());
        outHeader.setProtocolVersion(inHeader.getProtocolVersion());
        outHeader.setProviderNSA(inHeader.getProviderNSA());
        outHeader.setRequesterNSA(inHeader.getRequesterNSA());

        return outHeader;

    }




}
