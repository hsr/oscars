
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package net.es.oscars.api.http;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLoggerize;
import org.apache.log4j.Logger;

import net.es.oscars.api.forwarder.ForwarderFactory;
import net.es.oscars.api.forwarder.Forwarder;
import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.soap.OSCARSFaultUtils;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

@OSCARSNetLoggerize(moduleName = ModuleName.INTAPI)
@javax.jws.WebService(
        serviceName = ServiceNames.SVC_API_INTERNAL,
        portName = "OSCARSInternalPortType",
        targetNamespace = "http://oscars.es.net/OSCARS/06",
        endpointInterface = "net.es.oscars.api.soap.gen.v06.OSCARSInternalPortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")

/* Handles the forwarding of messages for interdomain reservations. Receives messages
 * from the Coordinator:InternalAPIWorker  and forwards them to the next domain.
 * The ForwarderFactory class determines what protocol the next domain is using and returns
 * a Forwarder that will do dataTranslation if required.
 */

public class OSCARSInternalSoapHandler implements OSCARSInternalPortType {

    private static final Logger LOG = Logger.getLogger(OSCARSInternalPortTypeImpl.class.getName());
    private static final String moduleName = ModuleName.INTAPI;


    /**
      *  Forwards a createReservation request to the next domain
      * @param createReservation
      * @param destDomainId
      * @return
      * @throws OSCARSFaultMessage
      */
    public CreateReply createReservation(ResCreateContent createReservation,String destDomainId)
             throws OSCARSFaultMessage {
         String event = "createReservation";
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
         netLogger.init(moduleName, createReservation.getMessageProperties().getGlobalTransactionId());
         netLogger.setGRI(createReservation.getGlobalReservationId());
         LOG.info(netLogger.start(event, "FORWARD " + event + " " +
                                  createReservation.getGlobalReservationId() +
                                  " to " + destDomainId));
         Forwarder forwarder;
         try {
             forwarder = ForwarderFactory.getForwarder(destDomainId);
         } catch (OSCARSServiceException e) {
             throw new OSCARSFaultMessage (e.toString());
         }

         if (forwarder == null) {
             throw new OSCARSFaultMessage ("no forwarder for " + destDomainId);
         }

         try {
             return forwarder.createReservation(createReservation);
         } catch (OSCARSServiceException ex) {
             OSCARSFaultUtils.handleError(ex, true, null, LOG, event);
         } catch (Exception ex) {
             OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
         }
         LOG.info(netLogger.end(event));
         return null;
    }
         /**
      * Forwards a modifyReservation request
      * @param modifyReservation
      * @param destDomainId
      * @return
      * @throws OSCARSFaultMessage
      */
    public ModifyResReply modifyReservation(ModifyResContent modifyReservation,String destDomainId)
             throws OSCARSFaultMessage    {
         String event = "modifyReservation";
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
         netLogger.init(moduleName, modifyReservation.getMessageProperties().getGlobalTransactionId());
         netLogger.setGRI(modifyReservation.getGlobalReservationId());
         LOG.info(netLogger.start(event, "FORWARD " + event + " " +
                                         modifyReservation.getGlobalReservationId() +
                                         "  to " + destDomainId));

         Forwarder forwarder;
         try {
             forwarder = ForwarderFactory.getForwarder(destDomainId);
         } catch (OSCARSServiceException e) {
             throw new OSCARSFaultMessage (e.toString());
         }

         if (forwarder == null) {
             throw new OSCARSFaultMessage ("no forwarder for " + destDomainId);
         }

         try {
             return forwarder.modifyReservation (modifyReservation);

         } catch (OSCARSServiceException ex) {
             OSCARSFaultUtils.handleError(ex, true, null, LOG, event);
         } catch (Exception ex) {
             OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
         }
         LOG.info(netLogger.end(event));
         return null;
     }
    /**
     *   Forwards a cancelReservation request to the  next domain
     * @param cancelReservation
     * @param destDomainId
     * @return
     * @throws OSCARSFaultMessage
     */
    public CancelResReply cancelReservation(CancelResContent cancelReservation,String destDomainId)
            throws OSCARSFaultMessage    {
        String event = "cancelReservation";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName, cancelReservation.getMessageProperties().getGlobalTransactionId());
        netLogger.setGRI(cancelReservation.getGlobalReservationId());
        LOG.info(netLogger.start(event, "FORWARD " + event + " " +
                                         cancelReservation.getGlobalReservationId() +
                                        " to " + destDomainId));
        Forwarder forwarder;
        try {
            forwarder = ForwarderFactory.getForwarder(destDomainId);
        } catch (OSCARSServiceException e) {
            throw new OSCARSFaultMessage (e.toString());
        }

        if (forwarder == null) {
            throw new OSCARSFaultMessage ("no forwarder for " + destDomainId);
        }

        try {
            return forwarder.cancelReservation (cancelReservation);
        } catch (OSCARSServiceException ex) {
            OSCARSFaultUtils.handleError(ex, true, null, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
        }
        LOG.info(netLogger.end(event));
        return null;
    }


    /**
     *  forwards a createPath request to the next domain
     * @param createPath
     * @param destDomainId
     * @return
     * @throws OSCARSFaultMessage
     */
    public CreatePathResponseContent createPath(CreatePathContent createPath,String destDomainId)
            throws OSCARSFaultMessage    {
        String event = "createPath";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName, createPath.getMessageProperties().getGlobalTransactionId());
        netLogger.setGRI(createPath.getGlobalReservationId());
        LOG.info(netLogger.start(event, "FORWARD " + event + " " +
                                         createPath.getGlobalReservationId() +
                                         " to " + destDomainId));
        Forwarder forwarder;
        try {
            forwarder = ForwarderFactory.getForwarder(destDomainId);
        } catch (OSCARSServiceException e) {
            throw new OSCARSFaultMessage (e.toString());
        }

        if (forwarder == null) {
            throw new OSCARSFaultMessage ("no forwarder for " + destDomainId);
        }

        try {
            return forwarder.createPath(createPath);
        } catch (OSCARSServiceException ex) {
            OSCARSFaultUtils.handleError(ex, true, null, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
        }
        LOG.info(netLogger.end(event));
        return null;
    }

    /**
     *  Forwards a teardownPath request to next domain
     * @param teardownPath
     * @param destDomainId
     * @return
     * @throws OSCARSFaultMessage
     */
    public TeardownPathResponseContent teardownPath(TeardownPathContent teardownPath,
                                                    String destDomainId)
            throws OSCARSFaultMessage    {
        String event = "teardownPath";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName, teardownPath.getMessageProperties().getGlobalTransactionId());
        netLogger.setGRI(teardownPath.getGlobalReservationId());
        LOG.info(netLogger.start(event, "FORWARD " + event + " " +
                                         teardownPath.getGlobalReservationId() +
                                        " to " + destDomainId));
        Forwarder forwarder;
        try {
            forwarder = ForwarderFactory.getForwarder(destDomainId);
        } catch (OSCARSServiceException e) {
            throw new OSCARSFaultMessage (e.toString()); 
        }
        
        if (forwarder == null) {
            throw new OSCARSFaultMessage ("no forwarder for " + destDomainId);
        }
        
        try {
            return forwarder.teardownPath (teardownPath);
        } catch (OSCARSServiceException ex) {
            OSCARSFaultUtils.handleError(ex, true, null, LOG, event);
        } catch (Exception ex) {
            OSCARSFaultUtils.handleError( ex, false, null, LOG, event);
        }
        LOG.info(netLogger.end(event));
        return null;
    }


    /**
     * Sends an interDomainEvent to the specified peer IDC
     * @param interDomainEvent
     * @param destDomainId
     */
    public void interDomainEvent(InterDomainEventContent interDomainEvent,String destDomainId) {
        String event = "interDomainEvent";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        if (interDomainEvent.getMessageProperties() != null) {
            netLogger.init(moduleName, interDomainEvent.getMessageProperties().getGlobalTransactionId());
        } else { // shouldn't happen
            netLogger.init(moduleName, null);
        }
        String gri = null;
        if (interDomainEvent.getResDetails() !=  null) {
            gri = interDomainEvent.getResDetails().getGlobalReservationId();
            netLogger.setGRI(gri);
        }
        LOG.info(netLogger.start(event, "send " + interDomainEvent.getType() + " to " + destDomainId));
        Forwarder forwarder = null;
        try {
            forwarder = ForwarderFactory.getForwarder(destDomainId);
        } catch (OSCARSServiceException e) {
            LOG.error(netLogger.end(event,"caught exception " +e.toString()));
        }

        if (forwarder == null) {
            LOG.error(netLogger.end(event, "no forwarder for " + destDomainId));
        }

        try {
            forwarder.notify(interDomainEvent);
        } catch (OSCARSServiceException e) {
            // IDEs do not reply
            LOG.error(netLogger.end(event, "caught exception " + e.toString()));
        }
        LOG.info(netLogger.end(event, "sent " + interDomainEvent.getType()) + " to " + destDomainId);
    }

    /*
     *  QueryReservation is not forwarded
     */
    public QueryResReply queryReservation(QueryResContent queryReservation,String destDomainId)
            throws net.es.oscars.common.soap.gen.OSCARSFaultMessage    {
        String event = "queryReservation";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        LOG.error(netLogger.start(event, queryReservation.getGlobalReservationId() +
                                  " to " + destDomainId));
        Thread.dumpStack();
        throw new OSCARSFaultMessage("Attempting to forward a queryReservation Request");
    }

    /*
     *  getErrorReport is not forwarded
     */
    public GetErrorReportResponseContent getErrorReport(GetErrorReportContent getErrorReportReq,
                                                        String destDomainId)
            throws net.es.oscars.common.soap.gen.OSCARSFaultMessage    {

        String event = "getErrorReport";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        LOG.info(netLogger.start(event, " to " + destDomainId));
        Thread.dumpStack();
        throw new OSCARSFaultMessage("Attempting to forward a getErrorReport Request");
    }

    /*
     *   listReservations is not forwarded
     */
    public net.es.oscars.api.soap.gen.v06.ListReply listReservations(ListRequest listReservations,java.lang.String destDomainId) throws net.es.oscars.common.soap.gen.OSCARSFaultMessage    { 
        LOG.info("Executing operation listReservations");
        System.out.println(listReservations);
        System.out.println(destDomainId);
        Thread.dumpStack();
        throw new OSCARSFaultMessage("Attempting to forward a listReservations Request");
    }

    /*
     *  Not implemented
     */
    public RefreshPathResponseContent refreshPath(RefreshPathContent refreshPath,String destDomainId)
            throws net.es.oscars.common.soap.gen.OSCARSFaultMessage    {
        LOG.info("Executing forward operation refreshPath");
        Thread.dumpStack();
        throw new OSCARSFaultMessage("RefreshPath not implemented");
    }

    /*
    *    Not forwarded
    */
    public GetTopologyResponseContent getNetworkTopology(GetTopologyContent getNetworkTopology,
                                                         String destDomainId) throws OSCARSFaultMessage    {
        LOG.info("Executing operation getNetworkTopology");
        System.out.println(getNetworkTopology);
        System.out.println(destDomainId);
        Thread.dumpStack();
        throw new OSCARSFaultMessage("Attempting to forward a GetNetworkTopology Request");
    }


}
