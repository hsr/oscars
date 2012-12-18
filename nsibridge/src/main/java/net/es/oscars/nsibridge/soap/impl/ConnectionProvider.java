package net.es.oscars.nsibridge.soap.impl;

import net.es.oscars.nsibridge.beans.*;
import net.es.oscars.nsibridge.prov.RequestProcessor;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort;

import javax.jws.WebService;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.*;
import org.apache.log4j.Logger;

import javax.xml.ws.Holder;

@WebService(
                      serviceName = "ConnectionServiceProvider",
                      portName = "ConnectionServiceProviderPort",
                      targetNamespace = "http://schemas.ogf.org/nsi/2012/03/connection/provider",
                      wsdlLocation = "file:/Users/haniotak/ij/0_6_trunk/nsibridge/schema/nsi-2_0/ogf_nsi_connection_provider_v2_0.wsdl",
                      endpointInterface = "net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort")

public class ConnectionProvider implements ConnectionProviderPort {

    private static final Logger log = Logger.getLogger(ConnectionProvider.class.getName());

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



    public void query(QueryOperationType operation,
                      QueryFilterType queryFilter,
                      CommonHeaderType inHeader,
                      Holder<CommonHeaderType> outHeader) throws ServiceException    {
        log.info("Executing operation query");
        QueryRequest req = new QueryRequest();
        req.setOperation(operation);
        req.setQueryFilter(queryFilter);
        req.setInHeader(inHeader);

        try {
            RequestProcessor.getInstance().startQuery(req);
            CommonHeaderType outHeaderValue = req.getOutHeader();
            outHeader.value = outHeaderValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }








    public void queryFailed(ServiceExceptionType serviceException,
                            CommonHeaderType header,
                            Holder<CommonHeaderType> outHeader) throws ServiceException    {
        log.info("Executing operation queryFailed");
        log.debug(serviceException);
        log.debug(header);
        try {
            CommonHeaderType outHeaderValue = null;
            outHeader.value = outHeaderValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        throw new ServiceException("operation not supported");
    }

    public GenericAcknowledgmentType queryConfirmed(QueryConfirmedType queryConfirmed,
                                                    Holder<CommonHeaderType> header) throws ServiceException    {
        log.info("Executing operation queryConfirmed");
        log.debug(queryConfirmed);
        log.debug(header.value);
        try {
            GenericAcknowledgmentType _return = null;
            throw new ServiceException("operation not supported");

            // return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void modifyCheck(String connectionId,
                            ModifyRequestCriteriaType criteria,
                            CommonHeaderType header,
                            Holder<CommonHeaderType> outHeader) throws ServiceException    {
        log.info("Executing operation modifyCheck");
        log.debug(connectionId);
        log.debug(criteria);
        log.debug(header);
        try {
            CommonHeaderType outHeaderValue = null;
            outHeader.value = outHeaderValue;
            throw new ServiceException("operation not supported");

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }


    public void modify(String connectionId,
                       CommonHeaderType header,
                       Holder<CommonHeaderType> outHeader) throws ServiceException    {
        log.info("Executing operation modify");
        log.debug(connectionId);
        log.debug(header);
        try {
            CommonHeaderType outHeaderValue = null;
            outHeader.value = outHeaderValue;
            throw new ServiceException("operation not supported");

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void modifyCancel(String connectionId,
                             CommonHeaderType header,
                             Holder<CommonHeaderType> outHeader) throws ServiceException    {
        log.info("Executing operation modifyCancel");
        log.debug(connectionId);
        log.debug(header);
        try {
            CommonHeaderType outHeaderValue = null;
            outHeader.value = outHeaderValue;
            throw new ServiceException("operation not supported");

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

}
