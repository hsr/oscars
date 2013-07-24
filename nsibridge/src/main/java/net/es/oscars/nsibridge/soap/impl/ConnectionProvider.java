package net.es.oscars.nsibridge.soap.impl;

import net.es.oscars.nsibridge.beans.*;
import net.es.oscars.nsibridge.prov.RequestProcessor;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort;

import javax.jws.WebParam;
import javax.jws.WebService;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.types.*;
import org.apache.log4j.Logger;

import javax.xml.ws.Holder;

@WebService(
                      serviceName = "ConnectionServiceProvider",
                      portName = "ConnectionServiceProviderPort",
                      targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/provider",
                      wsdlLocation = "schema/2013_04/ConnectionService/ogf_nsi_connection_provider_v2_0.wsdl",
                      endpointInterface = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.provider.ConnectionProviderPort")

public class ConnectionProvider implements ConnectionProviderPort {
    private static final Logger log = Logger.getLogger(ConnectionProvider.class.getName());


    @Override
    public void reserve(@WebParam(mode = WebParam.Mode.INOUT, name = "connectionId", targetNamespace = "") Holder<String> connectionId,
                        @WebParam(name = "globalReservationId", targetNamespace = "") String globalReservationId,
                        @WebParam(name = "description", targetNamespace = "") String description,
                        @WebParam(name = "criteria", targetNamespace = "") ReservationRequestCriteriaType criteria,
                        @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType inHeader,
                        @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> outHeader)
            throws ServiceException {
        log.info("Executing operation reserve");

        ResvRequest req = new ResvRequest();
        req.setConnectionId(connectionId.value);
        req.setCriteria(criteria);
        req.setDescription(description);
        req.setGlobalReservationId(globalReservationId);
        req.setInHeader(inHeader);
        log.debug("connId: "+connectionId);


        try {
            RequestProcessor.getInstance().startReserve(req);
            CommonHeaderType outHeaderValue = req.getOutHeader();
            outHeader.value = outHeaderValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void reserveAbort(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void reserveCommit(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }




    @Override
    public void provision(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void release(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }



    @Override
    public void terminate(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public QueryNotificationConfirmedType queryNotificationSync(@WebParam(partName = "queryNotificationSync", name = "queryNotificationSync", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types") QueryNotificationType queryNotificationSync, @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header) throws QueryNotificationSyncFailed {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void queryNotification(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "startNotificationId", targetNamespace = "") Integer startNotificationId, @WebParam(name = "endNotificationId", targetNamespace = "") Integer endNotificationId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GenericAcknowledgmentType queryRecursive(@WebParam(partName = "queryRecursive", name = "queryRecursive", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types") QueryType queryRecursive, @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header) throws ServiceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GenericAcknowledgmentType querySummary(@WebParam(partName = "querySummary", name = "querySummary", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types") QueryType querySummary, @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header) throws ServiceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QuerySummaryConfirmedType querySummarySync(@WebParam(partName = "querySummarySync", name = "querySummarySync", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types") QueryType querySummarySync, @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header) throws QuerySummarySyncFailed {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /*

    public void reserve(String globalReservationId,
                        String description,
                        String connectionId,
                        ReservationRequestCriteriaType criteria,
                        CommonHeaderType inHeader,
                        Holder<CommonHeaderType> outHeader) throws ServiceException    {
        log.info("Executing operation reserve");

        ResvRequest req = new ResvRequest();
        req.setConnectionId(connectionId);
        req.setCriteria(criteria);
        req.setDescription(description);
        req.setGlobalReservationId(globalReservationId);
        req.setInHeader(inHeader);
        log.debug("connId: "+connectionId);


        try {
            RequestProcessor.getInstance().startReserve(req);
            CommonHeaderType outHeaderValue = req.getOutHeader();
            outHeader.value = outHeaderValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }


    public void provision(String connectionId,
                          CommonHeaderType inHeader,
                          Holder<CommonHeaderType> outHeader) throws ServiceException    {
        log.info("Executing operation provision");
        log.debug(connectionId);

        ProvRequest req = new ProvRequest();
        req.setConnectionId(connectionId);
        req.setInHeader(inHeader);
        try {
            RequestProcessor.getInstance().startProvision(req);
            CommonHeaderType outHeaderValue = req.getOutHeader();
            outHeader.value = outHeaderValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void terminate(String connectionId,
                          CommonHeaderType inHeader,
                          Holder<CommonHeaderType> outHeader) throws ServiceException    {
        log.info("Executing operation terminate");
        log.debug(connectionId);

        TermRequest req = new TermRequest();
        req.setConnectionId(connectionId);
        req.setInHeader(inHeader);

        try {
            RequestProcessor.getInstance().startTerminate(req);
            CommonHeaderType outHeaderValue = req.getOutHeader();
            outHeader.value = outHeaderValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }



    public void release(String connectionId,
                        CommonHeaderType inHeader,
                        Holder<CommonHeaderType> outHeader) throws ServiceException    {
        log.info("Executing operation release");
        log.debug(connectionId);

        RelRequest req = new RelRequest();
        req.setConnectionId(connectionId);
        req.setInHeader(inHeader);
        try {
            RequestProcessor.getInstance().startRelease(req);
            CommonHeaderType outHeaderValue = req.getOutHeader();
            outHeader.value = outHeaderValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    */

}
