package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.7.6
 * 2013-07-29T09:59:16.213-07:00
 * Generated source version: 2.7.6
 * 
 */
@WebService(targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/provider", name = "ConnectionProviderPort")
@XmlSeeAlso({net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.ObjectFactory.class, net.es.oscars.nsibridge.soap.gen.saml.assertion.ObjectFactory.class, net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ObjectFactory.class, net.es.oscars.nsibridge.soap.gen.xmldsig.ObjectFactory.class, net.es.oscars.nsibridge.soap.gen.xmlenc.ObjectFactory.class, net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.ObjectFactory.class})
public interface ConnectionProviderPort {

    /**
     * The provision message is sent from a Requester NSA to a Provider
     * NSA when an existing reservation is to be transitioned into a
     * provisioned state. The provisionACK indicates that the Provider
     * NSA has accepted the provision request for processing. A
     * provisionConfirmed message will be sent asynchronously to the
     * Requester NSA when provision processing has completed.  There is
     * no associated Failed message for this operation.
     *             
     */
    @RequestWrapper(localName = "provision", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.GenericRequestType")
    @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/provision")
    @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.GenericAcknowledgmentType")
    public void provision(
        @WebParam(name = "connectionId", targetNamespace = "")
        java.lang.String connectionId,
        @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType header,
        @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType> header1
    ) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;

    /**
     * The reserveCommit message is sent from a Requester NSA to a
     * Provider NSA when a reservation or modification to an existing
     * reservation is being committed. The reserveCommitACK indicates
     * that the Provider NSA has accepted the modify request for
     * processing. A reserveCommitConfirmed or reserveCommitFailed message
     * will be sent asynchronously to the Requester NSA when reserve
     * or modify processing has completed.
     *             
     */
    @RequestWrapper(localName = "reserveCommit", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.GenericRequestType")
    @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/reserveCommit")
    @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.GenericAcknowledgmentType")
    public void reserveCommit(
        @WebParam(name = "connectionId", targetNamespace = "")
        java.lang.String connectionId,
        @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType header,
        @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType> header1
    ) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;

    /**
     * The reserve message is sent from a Requester NSA to a Provider
     * NSA when a new reservation is being requested, or a modification
     * to an existing reservation is required. The reserveResponse
     * indicates that the Provider NSA has accepted the reservation
     * request for processing and has assigned it the returned
     * connectionId. A reserveConfirmed or reserveFailed message will
     * be sent asynchronously to the Requester NSA when reserve 
     * operation has completed processing.
     *             
     */
    @RequestWrapper(localName = "reserve", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReserveType")
    @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/reserve")
    @ResponseWrapper(localName = "reserveResponse", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReserveResponseType")
    public void reserve(
        @WebParam(mode = WebParam.Mode.INOUT, name = "connectionId", targetNamespace = "")
        javax.xml.ws.Holder<java.lang.String> connectionId,
        @WebParam(name = "globalReservationId", targetNamespace = "")
        java.lang.String globalReservationId,
        @WebParam(name = "description", targetNamespace = "")
        java.lang.String description,
        @WebParam(name = "criteria", targetNamespace = "")
        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReservationRequestCriteriaType criteria,
        @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType header,
        @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType> header1
    ) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;

    /**
     * The queryRecursive message can be sent from either a Provider or
     * Requester NSA to determine the status of existing reservations.
     * The queryRecursiveACK indicates that the target NSA has accepted
     * the queryRecursive request for processing. A queryRecursiveConfirmed
     * or queryRecursiveFailed message will be sent asynchronously to the
     * requesting NSA when query processing has completed.
     *             
     */
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", partName = "acknowledgment")
    @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/queryRecursive")
    public net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.GenericAcknowledgmentType queryRecursive(
        @WebParam(partName = "queryRecursive", name = "queryRecursive", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types")
        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QueryType queryRecursive,
        @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType> header
    ) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;

    /**
     * The reserveAbort message is sent from a Requester NSA to a
     * Provider NSA when a cancellation to an existing reserve or
     * modify operation is being requested. The reserveAbortACK
     * indicates that the Provider NSA has accepted the reserveAbort
     * request for processing. A reserveAbortConfirmed or
     * reserveAbortFailed message will be sent asynchronously to the
     * Requester NSA when reserveAbort processing has completed.
     *             
     */
    @RequestWrapper(localName = "reserveAbort", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.GenericRequestType")
    @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/reserveAbort")
    @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.GenericAcknowledgmentType")
    public void reserveAbort(
        @WebParam(name = "connectionId", targetNamespace = "")
        java.lang.String connectionId,
        @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType header,
        @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType> header1
    ) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;

    /**
     * The queryNotificationSync message can be sent from a Requester NSA
     * to notifications against an existing reservations on the Provider
     * NSA. The queryNotificationSync is a synchronous operation that
     * will block until the results of the query operation have been
     * collected.  These results will be returned in the SOAP response.
     *             
     */
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "queryNotificationSyncConfirmed", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", partName = "queryNotificationSyncConfirmed")
    @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/queryNotificationSync")
    public net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QueryNotificationConfirmedType queryNotificationSync(
        @WebParam(partName = "queryNotificationSync", name = "queryNotificationSync", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types")
        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QueryNotificationType queryNotificationSync,
        @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType> header
    ) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.QueryNotificationSyncFailed;

    /**
     * The queryNotification message is sent from a Requester NSA
     * to a Provider NSA to retrieve notifications against an existing
     * reservation residing on the Provider NSA. QueryNotification is an
     * asynchronous operation that will return results of the operation
     * to the Requester NSA's SOAP endpoint specified in the NSI header
     * replyTo field.
     *             
     */
    @RequestWrapper(localName = "queryNotification", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QueryNotificationType")
    @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/queryNotification")
    @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.GenericAcknowledgmentType")
    public void queryNotification(
        @WebParam(name = "connectionId", targetNamespace = "")
        java.lang.String connectionId,
        @WebParam(name = "startNotificationId", targetNamespace = "")
        java.lang.Integer startNotificationId,
        @WebParam(name = "endNotificationId", targetNamespace = "")
        java.lang.Integer endNotificationId,
        @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType header,
        @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType> header1
    ) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;

    /**
     * The release message is sent from a Requester NSA to a Provider
     * NSA when an existing reservation is to be transitioned into a
     * released state. The releaseACK indicates that the Provider NSA
     * has accepted the release request for processing. A
     * releaseConfirmed message will be sent asynchronously to the
     * Requester NSA when release processing has completed.  There is
     * no associated Failed message for this operation.
     *             
     */
    @RequestWrapper(localName = "release", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.GenericRequestType")
    @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/release")
    @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.GenericAcknowledgmentType")
    public void release(
        @WebParam(name = "connectionId", targetNamespace = "")
        java.lang.String connectionId,
        @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType header,
        @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType> header1
    ) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;

    /**
     * The terminate message is sent from a Requester NSA to a Provider
     * NSA when an existing reservation is to be terminated. The
     * terminateACK indicates that the Provider NSA has accepted the
     * terminate request for processing. A terminateConfirmed or
     * terminateFailed message will be sent asynchronously to the Requester
     * NSA when terminate processing has completed.
     *             
     */
    @RequestWrapper(localName = "terminate", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.GenericRequestType")
    @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/terminate")
    @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", className = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.GenericAcknowledgmentType")
    public void terminate(
        @WebParam(name = "connectionId", targetNamespace = "")
        java.lang.String connectionId,
        @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType header,
        @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType> header1
    ) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;

    /**
     * The querySummary message is sent from a Requester NSA to a
     * Provider NSA to determine the status of existing reservations.
     * The querySummaryACK indicates that the target NSA has
     * accepted the querySummary request for processing. A
     * querySummaryConfirmed or querySummaryFailed message will be
     * sent asynchronously to the requesting NSA when querySummary
     * processing has completed.
     *             
     */
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", partName = "acknowledgment")
    @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/querySummary")
    public net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.GenericAcknowledgmentType querySummary(
        @WebParam(partName = "querySummary", name = "querySummary", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types")
        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QueryType querySummary,
        @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType> header
    ) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;

    /**
     * The querySummarySync message can be sent from a Requester NSA
     * to determine the status of existing reservations on the Provider
     * NSA. The querySummarySync is a synchronous operation that will
     * block until the results of the query operation have been
     * collected.  These results will be returned in the SOAP
     * response.
     *             
     */
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "querySummarySyncConfirmed", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types", partName = "querySummarySyncConfirmed")
    @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/querySummarySync")
    public net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryConfirmedType querySummarySync(
        @WebParam(partName = "querySummarySync", name = "querySummarySync", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types")
        net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.QueryType querySummarySync,
        @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true)
        javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType> header
    ) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.QuerySummarySyncFailed;
}
