
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package net.es.oscars.nsibridge.soap.impl;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.requester.ConnectionRequesterPort;
import javax.jws.WebService;
import java.util.logging.Logger;
import javax.xml.ws.Holder;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.*;

@WebService(
                      serviceName = "ConnectionServiceRequester",
                      portName = "ConnectionServiceRequesterPort",
                      targetNamespace = "http://schemas.ogf.org/nsi/2012/03/connection/requester",
                      wsdlLocation = "file:/Users/haniotak/ij/0_6_trunk/nsibridge/schema/nsi-2_0/ogf_nsi_connection_requester_v2_0.wsdl",
                      endpointInterface = "net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.requester.ConnectionRequesterPort")

public class ConnectionRequester implements ConnectionRequesterPort {

    private static final Logger LOG = Logger.getLogger(ConnectionRequester.class.getName());

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



    public GenericAcknowledgmentType queryConfirmed(QueryConfirmedType queryConfirmed,
                                                    Holder<CommonHeaderType> header) throws ServiceException    {
        LOG.info("Executing operation queryConfirmed");
        System.out.println(queryConfirmed);
        System.out.println(header.value);
        try {
            GenericAcknowledgmentType _return = new GenericAcknowledgmentType();
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void queryFailed(ServiceExceptionType serviceException,
                            CommonHeaderType header,
                            Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation queryFailed");
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








    public void modifyFailed(String globalReservationId,String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.ConnectionStatesType connectionStates,net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType serviceException,CommonHeaderType header,javax.xml.ws.Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation modifyFailed");
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

    public void modifyConfirmed(String globalReservationId,String connectionId,CommonHeaderType header,javax.xml.ws.Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation modifyConfirmed");
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


    public void modifyCancelFailed(String globalReservationId,String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.ConnectionStatesType connectionStates,net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType serviceException,CommonHeaderType header,javax.xml.ws.Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation modifyCancelFailed");
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




    public void modifyCancelConfirmed(String globalReservationId,String connectionId,CommonHeaderType header,javax.xml.ws.Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation modifyCancelConfirmed");
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



    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.requester.ConnectionRequesterPort#query(net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.QueryOperationType  operation ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.QueryFilterType  queryFilter ,)CommonHeaderType  header ,)CommonHeaderType  header1 )*
     */
    public void query(net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.QueryOperationType operation,net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.QueryFilterType queryFilter,CommonHeaderType header,javax.xml.ws.Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation query");
        System.out.println(operation);
        System.out.println(queryFilter);
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



    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.requester.ConnectionRequesterPort#modifyCheckFailed(java.lang.String  globalReservationId ,)java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.ConnectionStatesType  connectionStates ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType  serviceException ,)CommonHeaderType  header ,)CommonHeaderType  header1 )*
     */
    public void modifyCheckFailed(String globalReservationId,String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.ConnectionStatesType connectionStates,net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType serviceException,CommonHeaderType header,javax.xml.ws.Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation modifyCheckFailed");
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




    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.requester.ConnectionRequesterPort#modifyCheckConfirmed(net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.ModifyCheckConfirmedType  modifyCheckConfirmed ,)CommonHeaderType  header )*
     */
    public GenericAcknowledgmentType modifyCheckConfirmed(net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.ModifyCheckConfirmedType modifyCheckConfirmed,javax.xml.ws.Holder<CommonHeaderType> header) throws ServiceException    {
        LOG.info("Executing operation modifyCheckConfirmed");
        System.out.println(modifyCheckConfirmed);
        System.out.println(header.value);
        try {
            GenericAcknowledgmentType _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.requester.ConnectionRequesterPort#notification(net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.EventEnumType  event ,)java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.ConnectionStatesType  connectionStates ,)javax.xml.datatype.XMLGregorianCalendar  timeStamp ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.TypeValuePairListType  additionalInfo ,)CommonHeaderType  header ,)CommonHeaderType  header1 )*
     */
    public void notification(net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.EventEnumType event,String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.ConnectionStatesType connectionStates,javax.xml.datatype.XMLGregorianCalendar timeStamp,net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.TypeValuePairListType additionalInfo,CommonHeaderType header,javax.xml.ws.Holder<CommonHeaderType> header1) throws ServiceException    {
        LOG.info("Executing operation notification");
        System.out.println(event);
        System.out.println(connectionId);
        System.out.println(connectionStates);
        System.out.println(timeStamp);
        System.out.println(additionalInfo);
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



}
