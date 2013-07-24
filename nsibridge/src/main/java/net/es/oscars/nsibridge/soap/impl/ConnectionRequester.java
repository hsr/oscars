package net.es.oscars.nsibridge.soap.impl;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.requester.ConnectionRequesterPort;

import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.ifce.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.types.*;

@WebService(
        serviceName = "ConnectionServiceRequester",
        portName = "ConnectionServiceRequesterPort",
        targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/requester",
        wsdlLocation = "file:schema/2013_04/ogf_nsi_connection_requester_v2_0.wsdl",
        endpointInterface = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.connection.requester.ConnectionRequesterPort")

public class ConnectionRequester implements ConnectionRequesterPort {

    private static final Logger LOG = Logger.getLogger(ConnectionRequester.class.getName());



    @Override
    public void errorEvent(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "notificationId", targetNamespace = "") int notificationId, @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar timeStamp, @WebParam(name = "event", targetNamespace = "") EventEnumType event, @WebParam(name = "additionalInfo", targetNamespace = "") TypeValuePairListType additionalInfo, @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
    @Override
    public void messageDeliveryTimeout(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "notificationId", targetNamespace = "") int notificationId, @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar timeStamp, @WebParam(name = "correlationId", targetNamespace = "") String correlationId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }






    @Override
    public void reserveConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "globalReservationId", targetNamespace = "") String globalReservationId, @WebParam(name = "description", targetNamespace = "") String description, @WebParam(name = "criteria", targetNamespace = "") ReservationConfirmCriteriaType criteria, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reserveTimeout(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "notificationId", targetNamespace = "") int notificationId, @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar timeStamp, @WebParam(name = "timeoutValue", targetNamespace = "") int timeoutValue, @WebParam(name = "originatingConnectionId", targetNamespace = "") String originatingConnectionId, @WebParam(name = "originatingNSA", targetNamespace = "") String originatingNSA, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reserveFailed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "connectionStates", targetNamespace = "") ConnectionStatesType connectionStates, @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reserveCommitConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reserveCommitFailed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "connectionStates", targetNamespace = "") ConnectionStatesType connectionStates, @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reserveAbortConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }




    @Override
    public void provisionConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }



    @Override
    public void terminateConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void releaseConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void dataPlaneStateChange(@WebParam(name = "connectionId", targetNamespace = "") String connectionId, @WebParam(name = "notificationId", targetNamespace = "") int notificationId, @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar timeStamp, @WebParam(name = "dataPlaneStatus", targetNamespace = "") DataPlaneStatusType dataPlaneStatus, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }






    @Override
    public void queryNotificationFailed(@WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
    @Override
    public GenericAcknowledgmentType queryNotificationConfirmed(@WebParam(partName = "queryNotificationConfirmed", name = "queryNotificationConfirmed", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types") QueryNotificationConfirmedType queryNotificationConfirmed, @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header) throws ServiceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
    @Override
    public void querySummaryFailed(@WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void querySummaryConfirmed(@WebParam(name = "reservation", targetNamespace = "") List<QuerySummaryResultType> reservation, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void queryRecursiveFailed(@WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceException, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void queryRecursiveConfirmed(@WebParam(name = "reservation", targetNamespace = "") List<QueryRecursiveResultType> reservation, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) CommonHeaderType header, @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true) Holder<CommonHeaderType> header1) throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }







    /*
    public void releaseConfirmed(String globalReservationId,
                                 String connectionId,
                                 CommonHeaderType header,
                                 Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation releaseConfirmed");
        System.out.println(globalReservationId);
        System.out.println(connectionId);
        System.out.println(header);
        try {
            CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new ServiceException("serviceException...");
    }

    public void releaseFailed(String globalReservationId,
                              String connectionId,
                              ConnectionStatesType connectionStates,
                              ServiceExceptionType serviceException,
                              CommonHeaderType header,
                              Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation releaseFailed");
        System.out.println(globalReservationId);
        System.out.println(connectionId);
        System.out.println(connectionStates);
        System.out.println(serviceException);
        System.out.println(header);
        try {
            CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new ServiceException("serviceException...");
    }


    public void terminateConfirmed(String globalReservationId,
                                   String connectionId,
                                   CommonHeaderType header,
                                   Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation terminateConfirmed");
        System.out.println(globalReservationId);
        System.out.println(connectionId);
        System.out.println(header);
        try {
            CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new ServiceException("serviceException...");
    }

    public void terminateFailed(String globalReservationId,
                                String connectionId,
                                ConnectionStatesType connectionStates,
                                ServiceExceptionType serviceException,
                                CommonHeaderType header,
                                Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation terminateFailed");
        System.out.println(globalReservationId);
        System.out.println(connectionId);
        System.out.println(connectionStates);
        System.out.println(serviceException);
        System.out.println(header);
        try {
            CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new ServiceException("serviceException...");
    }


    public GenericAcknowledgmentType reserveConfirmed(ReserveConfirmedType reserveConfirmed,
                                                      Holder<CommonHeaderType> header) throws ServiceException    {
        LOG.info("Executing operation reserveConfirmed");
        System.out.println(reserveConfirmed);
        System.out.println(header.value);
        try {
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void reserveFailed(String globalReservationId,
                              String connectionId,
                              ConnectionStatesType connectionStates,
                              ServiceExceptionType serviceException,
                              CommonHeaderType header,
                              Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation reserveFailed");
        System.out.println(globalReservationId);
        System.out.println(connectionId);
        System.out.println(connectionStates);
        System.out.println(serviceException);
        System.out.println(header);
        try {
            CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new ServiceException("serviceException...");
    }




    public void provisionConfirmed(String globalReservationId,
                                   String connectionId,
                                   CommonHeaderType header,
                                   Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation provisionConfirmed");
        System.out.println(globalReservationId);
        System.out.println(connectionId);
        System.out.println(header);
        try {
            CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new ServiceException("serviceException...");
    }

    public void provisionFailed(String globalReservationId,
                                String connectionId,
                                ConnectionStatesType connectionStates,
                                ServiceExceptionType serviceException,
                                CommonHeaderType header,
                                Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation provisionFailed");
        System.out.println(globalReservationId);
        System.out.println(connectionId);
        System.out.println(connectionStates);
        System.out.println(serviceException);
        System.out.println(header);
        try {
            CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new ServiceException("serviceException...");
    }
    */

}