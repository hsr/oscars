package net.es.oscars.api.http;

import java.util.UUID;

import net.es.oscars.api.soap.gen.v05.*;
import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;

import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.api.compat.DataTranslator05;
import net.es.oscars.utils.soap.OSCARSServiceException;

import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.validator.DataValidator;
import net.es.oscars.api.compat.ForwardTypes;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;

import javax.xml.ws.WebServiceContext;

@OSCARSNetLoggerize(moduleName = ModuleName.API)
@javax.jws.WebService(
		serviceName = ServiceNames.SVC_API,
		portName = "OSCARS",
		targetNamespace = "http://oscars.es.net/OSCARS",
		endpointInterface = "net.es.oscars.api.soap.gen.v05.OSCARS")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class OSCARSSoapHandler05 implements OSCARS {

	@javax.annotation.Resource
	private WebServiceContext myContext;

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(OSCARSSoapHandler06.class.getName());
	private static final String moduleName = ModuleName.API;

	/* (non-Javadoc)
	 * @see net.es.oscars.api.soap.gen.v05.OSCARS#queryReservation(net.es.oscars.api.soap.gen.v05.GlobalReservationId  queryReservation )*
	 */
	public net.es.oscars.api.soap.gen.v05.ResDetails queryReservation(GlobalReservationId queryReservation) throws BSSFaultMessage , AAAFaultMessage    { 
		String event = "queryReservation05";
		OSCARSNetLogger netLogger = new OSCARSNetLogger();
		netLogger.setGRI(queryReservation.getGri());
		LOG.info(netLogger.start(event));
		try {
			QueryResContent req06 = new QueryResContent();
			req06.setGlobalReservationId(queryReservation.getGri());
			QueryResReply reply06 = OSCARSSoapHandler06.queryReservation(req06, this.myContext);
			net.es.oscars.api.soap.gen.v05.ResDetails resDetails = DataTranslator05.translate(reply06);
			LOG.info(netLogger.end(event));
			return resDetails;
		} catch (OSCARSFaultMessage fault06) {
			fault06.printStackTrace();
			LOG.error(netLogger.error(event, ErrSev.MAJOR, fault06.getMessage()));
			BSSFaultMessage fault05 = new BSSFaultMessage(fault06.getMessage());
			throw fault05;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(netLogger.error(event, ErrSev.MAJOR, e.getMessage()));
			BSSFaultMessage fault05 = new BSSFaultMessage(e.getMessage());
			throw fault05;
		}   
	}

	/* (non-Javadoc)
	 * @see net.es.oscars.api.soap.gen.v05.OSCARS#createPath(net.es.oscars.api.soap.gen.v05.CreatePathContent  createPath )*
	 */
	public net.es.oscars.api.soap.gen.v05.CreatePathResponseContent createPath(CreatePathContent createPath)
			throws BSSFaultMessage , AAAFaultMessage    {
		return this.createPath(createPath, null);
	}

	public net.es.oscars.api.soap.gen.v05.CreatePathResponseContent createPath(CreatePathContent createPath, String src)
			throws BSSFaultMessage , AAAFaultMessage    {

		String event = "createPath05";
		OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
		LOG.info(netLogger.start(event, createPath.getGlobalReservationId()));

		net.es.oscars.api.soap.gen.v06.CreatePathContent createPath06 = new net.es.oscars.api.soap.gen.v06.CreatePathContent();
		try {
			createPath06 = DataTranslator05.translate (createPath);
		} catch (OSCARSServiceException e) {
			// handle
		}
		net.es.oscars.api.soap.gen.v06.CreatePathResponseContent createPathReply06 = null;
		try {
			createPathReply06 = OSCARSSoapHandler06.createPath (createPath06, this.myContext);
		} catch (OSCARSFaultMessage fault06) {
			// TODO: can we separate BSS fault from AAAFault ?
			BSSFaultMessage fault05 = new BSSFaultMessage(fault06.getMessage());
			throw fault05;
		}

		net.es.oscars.api.soap.gen.v05.CreatePathResponseContent createPathReply05 = new net.es.oscars.api.soap.gen.v05.CreatePathResponseContent();
		try {
			createPathReply05 =  DataTranslator05.translate (createPathReply06);
		} catch (OSCARSServiceException e) {
			// handle
		}
		LOG.info(netLogger.end(event, createPathReply05.getGlobalReservationId()));
		return createPathReply05;
	}

	/* (non-Javadoc)
	 * @see net.es.oscars.api.soap.gen.v05.OSCARS#createReservation(net.es.oscars.api.soap.gen.v05.ResCreateContent  createReservation )*
	 */
	public net.es.oscars.api.soap.gen.v05.CreateReply createReservation(ResCreateContent createReservation)
			throws BSSFaultMessage , AAAFaultMessage    {
		return this.createReservation(createReservation, null);
	}
	public net.es.oscars.api.soap.gen.v05.CreateReply createReservation(ResCreateContent createReservation, String src)
			throws BSSFaultMessage , AAAFaultMessage    {
		String event = "createReservation05";
		OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
		LOG.info(netLogger.start(event, createReservation.getGlobalReservationId()));

		net.es.oscars.api.soap.gen.v06.ResCreateContent resCreateContent06 = new net.es.oscars.api.soap.gen.v06.ResCreateContent();
		try {
			resCreateContent06 = DataTranslator05.translate (createReservation, src);
		} catch (OSCARSServiceException e) {
			// handle
		}
		//Verify data from original request is semantically valid and convert things like lookup service names
		DataValidator.validate(resCreateContent06, false);
		net.es.oscars.api.soap.gen.v06.CreateReply createReply06 = null;
		try {
			createReply06 = OSCARSSoapHandler06.createReservation(resCreateContent06, this.myContext);
		} catch (OSCARSFaultMessage fault06) {
			// TODO: can we separate BSS fault from AAAFault ?
			BSSFaultMessage fault05 = new BSSFaultMessage(fault06.getMessage());
			throw fault05;
		}

		net.es.oscars.api.soap.gen.v05.CreateReply createReply05 = new net.es.oscars.api.soap.gen.v05.CreateReply();
		try {
			createReply05 =  DataTranslator05.translate (createReply06);
		} catch (OSCARSServiceException e) {
			// handle
		}

		LOG.info(netLogger.end(event, createReply05.getGlobalReservationId()));
		return createReply05;
	}

	/* (non-Javadoc)
	 * @see net.es.oscars.api.soap.gen.v05.OSCARS#modifyReservation(net.es.oscars.api.soap.gen.v05.ModifyResContent  modifyReservation )*
	 */
	public net.es.oscars.api.soap.gen.v05.ModifyResReply modifyReservation(ModifyResContent modifyReservation) throws BSSFaultMessage , AAAFaultMessage {
		return this.modifyReservation(modifyReservation, null);
	}

	public net.es.oscars.api.soap.gen.v05.ModifyResReply modifyReservation(ModifyResContent modifyReservation, String src)
			throws BSSFaultMessage , AAAFaultMessage {
		String event = "modifyReservation05";
		OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
		LOG.info(netLogger.start(event, modifyReservation.getGlobalReservationId()));

		net.es.oscars.api.soap.gen.v06.ModifyResContent modifyReservation06 = new net.es.oscars.api.soap.gen.v06.ModifyResContent();
		try {
			modifyReservation06 = DataTranslator05.translate (modifyReservation, src);
		} catch (OSCARSServiceException e) {
			// handle
		}
		net.es.oscars.api.soap.gen.v06.ModifyResReply modifyReply06 = null;
		try {
			modifyReply06 = OSCARSSoapHandler06.modifyReservation(modifyReservation06, this.myContext);
		} catch (OSCARSFaultMessage fault06) {
			// TODO: can we separate BSS fault from AAAFault ?
			BSSFaultMessage fault05 = new BSSFaultMessage(fault06.getMessage());
			throw fault05;
		}

		net.es.oscars.api.soap.gen.v05.ModifyResReply modifyReply05 = new net.es.oscars.api.soap.gen.v05.ModifyResReply();
		try {
			modifyReply05 =  DataTranslator05.translate (modifyReply06, modifyReservation);
		} catch (OSCARSServiceException e) {
			// handle
		}

		LOG.info(netLogger.end(event, modifyReservation.getGlobalReservationId()));
		return modifyReply05;


	}


	/* (non-Javadoc)
	 * @see net.es.oscars.api.soap.gen.v05.OSCARS#listReservations(net.es.oscars.api.soap.gen.v05.ListRequest  listReservations )*
	 */
	public net.es.oscars.api.soap.gen.v05.ListReply listReservations(ListRequest listReservations) throws BSSFaultMessage , AAAFaultMessage    { 
		String event = "listReservations05";
		OSCARSNetLogger netLogger = new OSCARSNetLogger();
		LOG.info(netLogger.start(event));
		try {
			net.es.oscars.api.soap.gen.v06.ListRequest listRequest06 = DataTranslator05.translate(listReservations);
			ListReply listReply06 = OSCARSSoapHandler06.listReservations(listRequest06, this.myContext);
			net.es.oscars.api.soap.gen.v05.ListReply listReply05 = DataTranslator05.translate(listReply06);
			LOG.info(netLogger.end(event));
			return listReply05;
		} catch (OSCARSFaultMessage fault06) {
			fault06.printStackTrace();
			LOG.error(netLogger.error(event, ErrSev.MAJOR, fault06.getMessage()));
			BSSFaultMessage fault05 = new BSSFaultMessage(fault06.getMessage());
			throw fault05;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(netLogger.error(event, ErrSev.MAJOR, e.getMessage()));
			BSSFaultMessage fault05 = new BSSFaultMessage(e.getMessage());
			throw fault05;
		}   
	}

	/* (non-Javadoc)
	 * @see net.es.oscars.api.soap.gen.v05.OSCARS#teardownPath(net.es.oscars.api.soap.gen.v05.TeardownPathContent  teardownPath )*
	 */
	public net.es.oscars.api.soap.gen.v05.TeardownPathResponseContent teardownPath(TeardownPathContent teardownPath)
			throws BSSFaultMessage , AAAFaultMessage {
		return this.teardownPath(teardownPath, null);
	}
	public net.es.oscars.api.soap.gen.v05.TeardownPathResponseContent teardownPath(TeardownPathContent teardownPath, String src)
			throws BSSFaultMessage , AAAFaultMessage {

		String event = "teardownPath05";
		OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
		LOG.info(netLogger.start(event, teardownPath.getGlobalReservationId()));

		net.es.oscars.api.soap.gen.v06.TeardownPathContent teardownPath06 = new net.es.oscars.api.soap.gen.v06.TeardownPathContent();
		try {
			teardownPath06 = DataTranslator05.translate (teardownPath);
		} catch (OSCARSServiceException e) {
			// handle
		}
		net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent teardownPathReply06 = null;
		try {
			teardownPathReply06 = OSCARSSoapHandler06.teardownPath (teardownPath06, this.myContext);
		} catch (OSCARSFaultMessage fault06) {
			// TODO: can we separate BSS fault from AAAFault ?
			BSSFaultMessage fault05 = new BSSFaultMessage(fault06.getMessage());
			throw fault05;
		}

		net.es.oscars.api.soap.gen.v05.TeardownPathResponseContent teardownPathReply05 = new net.es.oscars.api.soap.gen.v05.TeardownPathResponseContent();
		try {
			teardownPathReply05 =  DataTranslator05.translate (teardownPathReply06);
		} catch (OSCARSServiceException e) {
			// handle
		}
		LOG.info(netLogger.end(event, teardownPathReply05.getGlobalReservationId()));
		return teardownPathReply05;
	}

	/* (non-Javadoc)
	 * @see net.es.oscars.api.soap.gen.v05.OSCARS#getNetworkTopology(net.es.oscars.api.soap.gen.v05.GetTopologyContent  getNetworkTopology )*
	 */
	public net.es.oscars.api.soap.gen.v05.GetTopologyResponseContent getNetworkTopology(GetTopologyContent getNetworkTopology) throws BSSFaultMessage , AAAFaultMessage    { 
		LOG.info("Executing operation getNetworkTopology");
		System.out.println(getNetworkTopology);
		try {
			net.es.oscars.api.soap.gen.v05.GetTopologyResponseContent _return = null;
			return _return;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		//throw new BSSFaultMessage("BSSFaultMessage...");
		//throw new AAAFaultMessage("AAAFaultMessage...");
	}

	/* (non-Javadoc)
	 * @see net.es.oscars.api.soap.gen.v05.OSCARS#forward(net.es.oscars.api.soap.gen.v05.Forward  forward )*
	 */
	public net.es.oscars.api.soap.gen.v05.ForwardReply forward(Forward forward) throws BSSFaultMessage , AAAFaultMessage    { 
		LOG.info("Executing operation forward");

		String contentType = forward.getPayload().getContentType();
		net.es.oscars.api.soap.gen.v05.ForwardReply forwardReply = new net.es.oscars.api.soap.gen.v05.ForwardReply();
		forwardReply.setContentType(contentType);

		if (ForwardTypes.CREATE_RESERVATION.equals(contentType)) {
			net.es.oscars.api.soap.gen.v05.CreateReply reply =
					this.createReservation(forward.getPayload().getCreateReservation(),
							forward.getPayloadSender());

			forwardReply.setCreateReservation(reply);
		} else if (ForwardTypes.CANCEL_RESERVATION.equals(contentType)) {
			String reply =
					this.cancelReservation(forward.getPayload().getCancelReservation(),
							forward.getPayloadSender());

			forwardReply.setCancelReservation(reply);
		} else if (ForwardTypes.MODIFY_RESERVATION.equals(contentType)) {
			net.es.oscars.api.soap.gen.v05.ModifyResReply reply =
					this.modifyReservation(forward.getPayload().getModifyReservation(),
							forward.getPayloadSender());

			forwardReply.setModifyReservation(reply);
		} else if (ForwardTypes.CREATE_PATH.equals(contentType)) {
			net.es.oscars.api.soap.gen.v05.CreatePathResponseContent reply =
					this.createPath(forward.getPayload().getCreatePath(),
							forward.getPayloadSender());

			forwardReply.setCreatePath(reply);
		} else if (ForwardTypes.TEARDOWN_PATH.equals(contentType)) {
			net.es.oscars.api.soap.gen.v05.TeardownPathResponseContent reply =
					this.teardownPath(forward.getPayload().getTeardownPath(),
							forward.getPayloadSender());

			forwardReply.setTeardownPath(reply);
		} else {
			// Invalid or not implemented forward message
			BSSFaultMessage fault05 = new BSSFaultMessage("Cannot handle 0.5 " + contentType + " 0.5 Forward message");
			throw fault05;
		}

		return forwardReply;
	}

	/* (non-Javadoc)
	 * @see net.es.oscars.api.soap.gen.v05.OSCARS#refreshPath(net.es.oscars.api.soap.gen.v05.RefreshPathContent  refreshPath )*
	 */
	public net.es.oscars.api.soap.gen.v05.RefreshPathResponseContent refreshPath(RefreshPathContent refreshPath) throws BSSFaultMessage , AAAFaultMessage    { 
		LOG.info("Executing operation refreshPath");
		System.out.println(refreshPath);
		try {
			net.es.oscars.api.soap.gen.v05.RefreshPathResponseContent _return = null;
			return _return;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		//throw new BSSFaultMessage("BSSFaultMessage...");
		//throw new AAAFaultMessage("AAAFaultMessage...");
	}

	/* (non-Javadoc)
	 * @see net.es.oscars.api.soap.gen.v05.OSCARS#cancelReservation(net.es.oscars.api.soap.gen.v05.GlobalReservationId  cancelReservation )*
	 */
	public java.lang.String cancelReservation(GlobalReservationId cancelReservation) throws BSSFaultMessage , AAAFaultMessage {
		return this.cancelReservation(cancelReservation, null);
	}
	public java.lang.String cancelReservation(GlobalReservationId cancelReservation, String src) throws BSSFaultMessage , AAAFaultMessage {
		String event = "cancelReservation05";
		OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
		LOG.info(netLogger.start(event, cancelReservation.getGri()));

		net.es.oscars.api.soap.gen.v06.CancelResContent cancelReservation06 = new net.es.oscars.api.soap.gen.v06.CancelResContent();
		try {
			cancelReservation06 = DataTranslator05.translate (cancelReservation);
		} catch (OSCARSServiceException e) {
			// handle
		}
		net.es.oscars.api.soap.gen.v06.CancelResReply cancelReservationReply06 = null;
		try {
			cancelReservationReply06 = OSCARSSoapHandler06.cancelReservation (cancelReservation06, this.myContext);
		} catch (OSCARSFaultMessage fault06) {
			// TODO: can we separate BSS fault from AAAFault ?
			BSSFaultMessage fault05 = new BSSFaultMessage(fault06.getMessage());
			throw fault05;
		}

		String cancelReservationReply05 = new String();
		try {
			cancelReservationReply05 = DataTranslator05.translate (cancelReservationReply06);
		} catch (OSCARSServiceException e) {
			// handle
		}

		LOG.info(netLogger.end(event, cancelReservation.getGri()));
		return cancelReservationReply05;

	}
}
