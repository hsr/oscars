package net.es.oscars.nsibridge.prov;

import net.es.oscars.api.soap.gen.v06.CancelResContent;
import net.es.oscars.api.soap.gen.v06.CancelResReply;
import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.authN.soap.gen.DNType;
import net.es.oscars.authN.soap.gen.VerifyDNReqType;
import net.es.oscars.authN.soap.gen.VerifyReply;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.nsibridge.config.oscars.OscarsConfig;
import net.es.oscars.utils.clients.AuthNClient;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.apache.log4j.Logger;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OscarsProxy {
    private static Logger log = Logger.getLogger(OscarsProxy.class);
    private CoordClient coordClient;
    private AuthNClient authNClient;
    private OscarsConfig oscarsConfig;


    private static OscarsProxy instance;



    public static OscarsProxy getInstance()  throws OSCARSServiceException {
        if (instance == null) instance = new OscarsProxy();
        return instance;

    }
    private OscarsProxy() {

    }

    public void initialize() throws OSCARSServiceException {
        this.initAuthNClient();
        this.initCoordClient();
    }


    public CancelResReply sendCancel(CancelResContent cancelReservation) throws OSCARSServiceException {
        MessagePropertiesType msgProps = cancelReservation.getMessageProperties();
        if (msgProps == null) {
            msgProps = this.makeMessageProps();
        }
        SubjectAttributes subjectAttributes = this.sendAuthNRequest(msgProps);
        msgProps = updateMessageProperties(msgProps, subjectAttributes);
        cancelReservation.setMessageProperties(msgProps);


        Object[] req = new Object[]{subjectAttributes, cancelReservation};

        if (oscarsConfig.isStub()) {
            System.out.println("stub mode, not contacting coordinator");
            CancelResReply cr = new CancelResReply();
            cr.setStatus("CANCELLED");
            return cr;
        } else {
            Object[] res = coordClient.invoke("cancelReservation", req);
            CancelResReply cr = (CancelResReply) res[0];
            return cr;
        }
    }

    public CreateReply sendCreate(ResCreateContent createReservation) throws OSCARSServiceException {
        MessagePropertiesType msgProps = createReservation.getMessageProperties();
        if (msgProps == null) {
            msgProps = this.makeMessageProps();
        }

        SubjectAttributes subjectAttributes = this.sendAuthNRequest(msgProps);
        msgProps = updateMessageProperties(msgProps, subjectAttributes);
        createReservation.setMessageProperties(msgProps);

        // Build the query
        Object[] req = new Object[]{subjectAttributes, createReservation};
        if (oscarsConfig.isStub()) {
            System.out.println("stub mode, not contacting coordinator");
            CreateReply cr = new CreateReply();
            cr.setGlobalReservationId(UUID.randomUUID().toString());
            cr.setStatus("RESERVED");
            return cr;
        } else {
            Object[] res = coordClient.invoke("createReservation",req);
            CreateReply cr = (CreateReply) res[0];
            return cr;
        }
    }








    private void initCoordClient() throws OSCARSServiceException {
        if (coordClient != null) {
            return;
        }
        ContextConfig cc = ContextConfig.getInstance();
        cc.setServiceName(ServiceNames.SVC_COORD);

        // not used yet, will be when reservations are managed
        String configFilename = null;
        try {
            configFilename = cc.getFilePath(ServiceNames.SVC_COORD ,cc.getContext(), ConfigDefaults.CONFIG);
        } catch (ConfigException e) {
            e.printStackTrace();
        }



        HashMap<String,Object> coordMap = (HashMap<String,Object>) ConfigHelper.getConfiguration(configFilename);
        if (coordMap == null) {
            throw new OSCARSServiceException("could not load coordinator config file "+configFilename);

        }
        Map soap = (HashMap<String,Object>) coordMap.get("soap");
        if (soap == null ) {
            throw new OSCARSServiceException("soap stanza not found in "+configFilename);
        }

        try {
            URL coordHost = new URL ((String)soap.get("publishTo"));
            URL coordWsdl = cc.getWSDLPath(ServiceNames.SVC_COORD,null);
            coordClient = CoordClient.getClient(coordHost, coordWsdl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void initAuthNClient() throws OSCARSServiceException {
        if (authNClient != null) {
            return;
        }

        ContextConfig cc = ContextConfig.getInstance();
        cc.setServiceName(ServiceNames.SVC_COORD);

        String configFilename = null;
        try {
            configFilename = cc.getFilePath(ServiceNames.SVC_AUTHN, cc.getContext(), ConfigDefaults.CONFIG);
        } catch (ConfigException e) {
            e.printStackTrace();
        }

        HashMap<String,Object> authNMap = (HashMap<String,Object>) ConfigHelper.getConfiguration(configFilename);
        if (authNMap == null) {
            throw new OSCARSServiceException("could not load authN config file "+configFilename);

        }
        Map soap = (HashMap<String,Object>) authNMap.get("soap");
        if (soap == null ) {
            throw new OSCARSServiceException("soap stanza not found in "+configFilename);
        }

        try {
            URL authNHost = new URL ((String)soap.get("publishTo"));
            URL authNWsdl = cc.getWSDLPath(ServiceNames.SVC_AUTHN,null);
            authNClient = AuthNClient.getClient(authNHost, authNWsdl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private MessagePropertiesType makeMessageProps() {
        MessagePropertiesType msgProps = new MessagePropertiesType();
        msgProps.setGlobalTransactionId(UUID.randomUUID().toString());
        return msgProps;
    }


    private SubjectAttributes sendAuthNRequest (MessagePropertiesType msgProps)
            throws OSCARSServiceException {

        String userDN   = this.oscarsConfig.getUserDN();
        String issuerDN = this.oscarsConfig.getIssuerDN();


        VerifyDNReqType verifyDNReq = new VerifyDNReqType();
        DNType DN = new DNType();
        DN.setSubjectDN(userDN);
        DN.setIssuerDN(issuerDN);
        verifyDNReq.setDN(DN);
        verifyDNReq.setTransactionId(msgProps.getGlobalTransactionId());
        Object[] req = new Object[]{verifyDNReq};
        SubjectAttributes subjectAttrs;
        if (oscarsConfig.isStub()) {
            System.out.println("stub mode, not contacting authN");
            subjectAttrs = new SubjectAttributes();
        } else {
            Object[] res = authNClient.invoke("verifyDN",req);
            VerifyReply reply = (VerifyReply)res[0];
            subjectAttrs = reply.getSubjectAttributes();
            if (subjectAttrs == null || subjectAttrs.getSubjectAttribute().isEmpty()){
                ErrorReport errRep = new ErrorReport (ErrorCodes.ACCESS_DENIED,
                        "no atributes for user " + userDN,
                        ErrorReport.USER);
                throw new OSCARSServiceException(errRep);
            }
        }
        return subjectAttrs;

    }



    private MessagePropertiesType updateMessageProperties (MessagePropertiesType msgProps, SubjectAttributes subjectAttributes) {
        SubjectAttributes originator;
        if (msgProps == null) {
            msgProps = new MessagePropertiesType();
        }
        String transId = msgProps.getGlobalTransactionId();
        if (transId == null || transId.equals("")) {
            transId = PathTools.getLocalDomainId() + "-NSI-" + UUID.randomUUID().toString();
            msgProps.setGlobalTransactionId(transId);
        }
        originator = msgProps.getOriginator();
        if ((originator == null) && (subjectAttributes != null)) {
            for (AttributeType att: subjectAttributes.getSubjectAttribute()) {
                if (att.getName().equals(AuthZConstants.LOGIN_ID)) {
                    originator = new SubjectAttributes();
                    originator.getSubjectAttribute().add(att);
                }
            }
            msgProps.setOriginator(originator);
        }
        return msgProps;
    }



    public OscarsConfig getOscarsConfig() {
        return oscarsConfig;
    }

    public void setOscarsConfig(OscarsConfig oscarsConfig) {
        System.out.println(oscarsConfig.toString());
        this.oscarsConfig = oscarsConfig;
    }

}
