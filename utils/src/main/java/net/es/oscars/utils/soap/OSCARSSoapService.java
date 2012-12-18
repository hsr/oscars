package net.es.oscars.utils.soap;

import java.lang.NullPointerException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;

import java.lang.reflect.Constructor;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.SharedConfig;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;


public class OSCARSSoapService <S,P> {

    private String      publishTo       = "";
    private URL         wsdlURL         = null;
    private P           portType        = null;
    private Service     service         = null;
    private Client      clientProxy     = null;
    private URL         host            = null;

    @SuppressWarnings("unchecked")
    private Map         config          = null;
    private String      namespace       = null;
    private String      serviceName     = null;
    private String      implementor     = null;
    private String      moduleName      = null;

    // too soon to initialize a logger, need to set cc.Context first
    private static Logger LOG = null;
    private OSCARSNetLogger netLogger = null;
    
    static public void setSSLBusConfiguration (URL keyStoreConf) {
        LOG=Logger.getLogger(OSCARSSoapService.class);
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        LOG.debug(netLogger.start("setSSLBusConfiguration", " using " + keyStoreConf.toString()));
         // The following prevents the default Java default cacerts from being added
        System.setProperty("javax.net.ssl.trustStore","DoNotUsecacerts");
        if (keyStoreConf != null) {
            SpringBusFactory bf = new SpringBusFactory();
            Bus bus = bf.createBus(keyStoreConf.toString());
            SpringBusFactory.setDefaultBus(bus);
        }
    }

    /**
     * OSCARSSoapService() is the constructor for server side implementation. This constructor
     * will retrieve the WSDL/XSD file path, binding URL and the implementation class for the
     * server side SOAP handler. Note that the constructor does not actually
     * create or start the service, the startServer() method must be used for this purpose.
     */
    public OSCARSSoapService(String module) throws OSCARSServiceException {
        LOG=Logger.getLogger(OSCARSSoapService.class);
        String event = "OSCARSSoapServiceInstantiated";
        this.netLogger = getOSCARSNetLogger();
        LOG.debug(netLogger.start(event, "service:" +  module));
        this.configurePublishedPaths(module);
    }
    /**
     * Constructor for client side of service.
     * @param host - host that the target service is running on
     * @param wsdlFile - wsdl for the target server
     * @param portTypeClass- portType for the target server
     * @throws OSCARSServiceException
     */
    public OSCARSSoapService (URL host, URL wsdlFile, Class<P> portTypeClass) throws OSCARSServiceException{
        LOG=Logger.getLogger(OSCARSSoapService.class);
        String event = "OSCARSSoapClientInstantiated";
        this.netLogger = OSCARSNetLogger.getTlogger();
        LOG.debug(this.netLogger.start(event,  null, host.toString(), null));
        try {
            this.wsdlURL = wsdlFile;
            this.service = getService();
            //LOG.debug("returned from getService");
            this.host = host;
            Iterator<QName> ports = this.service.getPorts();
            while (ports.hasNext()){
                // Assuming that there is only one port.
                this.portType = this.service.getPort (ports.next(),portTypeClass);
                //LOG.debug("returned from service.getPort");
                break;
            }
            this.clientProxy = ClientProxy.getClient(this.portType);
            // configureLoginToken();
        } catch (Exception e) {
            LOG.debug(this.netLogger.error(event, ErrSev.MAJOR,
                                        "Exception from ProxyClient.getClient " + e.getClass().getName() + 
                                        "  " + e.toString() + "  " + e.getMessage()));
            LOG.debug(this.netLogger.getMsg(event, "portType" + portTypeClass.getName() +
                    " host " + host.toString() + " wsdl " + wsdlFile.toString()));
            throw new OSCARSServiceException (e.toString());
        }
        LOG.debug(this.netLogger.end(event));
    }

    /**
     * Constructor
     * @param host
     * @param wsdlFile
     * @param portTypeClass
     * @param version
     * @param namespace
     * @param serviceName
     * @param implementor
     * @param configFile
     * @throws OSCARSServiceException

    public OSCARSSoapService (URL host,
                              URL wsdlFile,
                              Class<P> portTypeClass,
                              String version,
                              String namespace,
                              String serviceName,
                              String implementor,
                              String configFile) throws OSCARSServiceException {

        this.namespace = namespace;
        this.serviceName = serviceName;
        this.implementor = implementor;
        //this.configFile = configFile;
        this.wsdlURL = wsdlFile;
        this.service = getService();
        this.host = host;
        //LOG.debug("OSCARSSoapService2Instantiated for " + portTypeClass.getName());
        try {
            Iterator<QName> ports = this.service.getPorts();
            while (ports.hasNext()){
                // Assuming that there is only one port.
                this.portType = this.service.getPort (ports.next(),portTypeClass);
                //LOG.debug ("PORT " + this.portType);
                break;
            }
            this.clientProxy = ClientProxy.getClient(this.portType);
        } catch (Exception e) {
            throw new OSCARSServiceException (e);
        }
    } */

    /**
     * getOSCARSNetLogger
     *   This is here until all the services get NetLoggerized 
     * @return an initialized OSCARSNetLogger object
     */
    private  OSCARSNetLogger getOSCARSNetLogger() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        if (netLogger.getModuleName() == null ) {
            // netLogger has not been initialized by the current thread
            if (this.moduleName == null ) {
                if ( this.getClass().isAnnotationPresent(OSCARSNetLoggerize.class)) {
                    OSCARSNetLoggerize meta = this.getClass().getAnnotation (OSCARSNetLoggerize.class);
                    this.moduleName =  meta.moduleName();
                }
            }
            netLogger.init(this.moduleName, "0001");
            LOG.info(netLogger.start("OSCARSSoapService.getOscarsNetLogger"));
        }
        return netLogger;
    }
    /**
     *  getPortType
     * @return portType of service
     * @throws OSCARSServiceException
     */

    @SuppressWarnings("unchecked")
    public synchronized P getPortType () throws OSCARSServiceException {

        if (this.portType != null) {
            return this.portType;
        }

        if ( ! this.getClass().isAnnotationPresent(OSCARSService.class)) {
            if (this.implementor == null) {
                throw new OSCARSServiceException ("Missing OSCARSService annotation");
            }
        }
        try {
            if (this.implementor == null) {
                OSCARSService meta = this.getClass().getAnnotation (OSCARSService.class);
                this.implementor = meta.implementor();
            }

            if (this.implementor != "") {
                this.portType = (P) Class.forName(this.implementor).newInstance();
                return this.portType;
            } else {
                throw new OSCARSServiceException ("Missing OSCARSService annotation's implementor");
            }

        } catch (Exception e) {
            throw new OSCARSServiceException (e);
        }
    }

    /**
     * getService creates a service instance from annotations on this OSCARSservice
     * @return javax.xml.ws.Service associated with this OSCARSService
     * @throws OSCARSServiceException
     */
    @SuppressWarnings("unchecked")
    public synchronized Service getService() throws OSCARSServiceException {

        if (this.service != null) {
            return this.service;
        }
        if ( ! this.getClass().isAnnotationPresent(OSCARSService.class)) {
            if ((this.implementor == null) || (this.namespace == null) || (this.serviceName == null)) {
                throw new OSCARSServiceException ("Missing OSCARSService annotation");
            }
        }
        String event = "getService";
        try {
            if ((this.implementor == null) || (this.namespace == null) || (this.serviceName == null)) {
                OSCARSService meta = this.getClass().getAnnotation (OSCARSService.class);
                this.implementor = meta.implementor();
                this.namespace = meta.namespace();
                this.serviceName = meta.serviceName();
            }
            if (this.implementor != "") {
                 LOG.debug(this.netLogger.start(event,"implementor:"+ this.implementor +
                                                " namespace:" + this.namespace  +
                                                " serviceName:" + this.serviceName +
                                                " this.wsdlURL:" + this.wsdlURL.toString()));

                Class impl = Class.forName(this.implementor);
                Constructor c = impl.getConstructor(URL.class, QName.class);
                if (c == null) {
                    throw new OSCARSServiceException ("Invalid implementator");
                }
                LOG.debug(this.netLogger.getMsg( event, "creating Service from wsdl "+this.wsdlURL.toString() +
                                                 " ServiceName " + this.serviceName + 
                                                 " namespace " + this.namespace));
                this.service = (Service) c.newInstance(this.wsdlURL, new QName (this.namespace, this.serviceName));
                return this.service;
            } else {
                throw new OSCARSServiceException ("Missing OSCARSService annotation's implementor");
            }

        } catch (Exception e) {
            LOG.error(this.netLogger.error(event,ErrSev.MAJOR,"caught exception " + e.toString()));
            e.printStackTrace();
            throw new OSCARSServiceException (e.toString());
        }
        // LOG.debug(netLogger.end(event));
    }

    /**
     * startServer
     * @param authed - not used
     * @return the org.apache.cxf.jaxws.EndpointImpl for this service
     * @throws OSCARSServiceException
     */
    public Endpoint startServer(boolean authed) throws OSCARSServiceException {
        String event = "startServer";
        OSCARSNetLogger netLogger = getOSCARSNetLogger();
        if (this.publishTo != "") {
            LOG.info(netLogger.start(event, "started at " + this.publishTo));
            EndpointImpl jaxWsEndpoint = (EndpointImpl) Endpoint.publish(this.publishTo, this.getPortType());
            // org.apache.cxf.endpoint.Endpoint cxfEndpoint = jaxWsEndpoint.getServer().getEndpoint();
            return jaxWsEndpoint;
        } else {
            throw new OSCARSServiceException ("No publishing endpoint in configuration");
        }
    }

    /**
     * configurePublishedPaths gets the configuration for the subclass of OSCARSSoapService,
     * using the config file name and service name from its annotation. Saves the configuratin map
     * and gets the the publishTo port number from it.
     * Called from the server constructor.
     * Note: the config files are yaml files, not to be confused with the cxf.xml files.
     *
     * @throws OSCARSServiceException
     */
    @SuppressWarnings("unchecked")
    private void configurePublishedPaths(String module) throws OSCARSServiceException {

        String configAlias      = null;
        String serviceName      = null;
        if ( ! this.getClass().isAnnotationPresent(OSCARSService.class)) {
            throw new OSCARSServiceException ("Missing OSCARSService annotation");
        }
        try {
            OSCARSService sv = this.getClass().getAnnotation (OSCARSService.class);
            configAlias     = sv.config();
            serviceName     = sv.serviceName();
        } catch (NullPointerException ex) {
            throw new OSCARSServiceException ("Invalid OSCARSService annotation");
        }

        String configFile = null;
        String event = "configurePublishedPaths";

        ContextConfig cc = ContextConfig.getInstance(module);
        String configContext = cc.getContext();
        if (configContext == null) {
            LOG.debug(this.netLogger.error(event, ErrSev.MINOR,"configContext is null"));
            // Retrieve the deployment pathname of the configuration file
            try {
                configFile = (new SharedConfig(serviceName)).getFilePath(configAlias);
                //System.out.println("sc loading ["+serviceName+":"+":"+configAlias +"] config from: "+configFile);
            } catch (Exception e) {
                throw new OSCARSServiceException (e);
            }

        } else {
            // Retrieve the deployment pathname of the configuration file
            try {
                configFile = cc.getFilePath(serviceName, configContext, configAlias);
                //System.out.println("cc loading ["+serviceName+":"+configContext+":"+configAlias +"] config from: "+configFile);
                LOG.debug(this.netLogger.getMsg(event, "loading ["+serviceName+":"+configContext+":"+
                                           configAlias +"] config from: "+configFile));
            } catch (ConfigException e) {
                throw new OSCARSServiceException (e);
            }
        }
        // get configuration from the service yaml file
        this.config = ConfigHelper.getConfiguration(configFile);
        assert config != null : "No configuration";

        Map soap = (Map) this.config.get("soap");
        assert soap != null : "No soap stanza in configuration";
        this.publishTo = (String) soap.get("publishTo");
    }

    /**
     * getConfig
     * @return the configuration map
     */
    @SuppressWarnings("unchecked")
    public Map getConfig() {
        return this.config;
    }
    /**
     * Sends a message to the client proxy for this service
     * @param operation name of the operation to be called
     * @param params an array of object containing the parameters to the operation
     * @return an array of objects containing the returned parameters of the operations
     */
    @SuppressWarnings("unchecked")
    public Object[] invoke (String operation, Object[] params) throws OSCARSServiceException {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "invoke-client";
        try {
            // lomax@es.net: the following two line defines HashMap without using generics. Somehow, using generics on
            // the context map confuses CXF. A CXF bug report is going to be filled and this comment will be updated with the
            // tracking info.
            // TODO: add JIRA tracking info
            Map requestContext = new HashMap();
            Map context = new HashMap();
            
            context.put(Client.REQUEST_CONTEXT, requestContext);
            requestContext.put(Message.ENDPOINT_ADDRESS, this.host.toString());

            QName qName = new QName(this.getService().getServiceName().getNamespaceURI(), operation);
            BindingOperationInfo op = this.clientProxy.getEndpoint().getEndpointInfo().getBinding().getOperation(qName);
            if (op == null) {
                throw new OSCARSServiceException (operation + " operation is not defined in service's WSDL");
            }
            LOG.info(netLogger.start(event,operation,this.host.toString()));
            Object[] res = this.clientProxy.invoke(op, params, context);
            LOG.debug(netLogger.end(event,operation, this.host.toString()));
            return res;
        } catch (OSCARSServiceException oEx) {
            throw oEx;
        } catch (OSCARSFaultMessage oFm)  {
            ErrorReport errRep = ErrorReport.fault2report(oFm.getFaultInfo().getErrorReport());
            throw new OSCARSServiceException(errRep);
        } catch (Exception e) {
            String newMessage = "OSCARSSoapService.invoke:Exception connecting to " +
            operation + " on " + this.host.toString() +
            " Message is: " + e.getMessage();
            ErrorReport errRep = new ErrorReport(ErrorCodes.COULD_NOT_CONNECT,
                                                 newMessage,
                                                 ErrorReport.SYSTEM,
                                                 netLogger.getGRI(),
                                                 netLogger.getGUID(),
                                                 System.currentTimeMillis()/1000L,
                                                 netLogger.getModuleName(),
                                                 PathTools.getLocalDomainId());
            LOG.warn(netLogger.error(event, ErrSev.MINOR, newMessage));
            throw  new OSCARSServiceException(errRep);
        }

    }
}
