package net.es.oscars.coord;

import static java.util.Arrays.asList;

import java.net.URL;
import java.util.List;
import java.util.Map;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import net.es.oscars.utils.soap.ErrorReport;
import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.api.soap.gen.v06.GlobalReservationId;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.api.soap.gen.v06.CreatePathContent;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.api.soap.gen.v06.Layer3Info;
import net.es.oscars.api.soap.gen.v06.Layer2Info;
import net.es.oscars.api.soap.gen.v06.PathInfo;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

public final class coordServerTest {
    public static String request = null;
    public static String gri = null;
    public static Integer numReq = null;
    public static Integer offSet = null;
    public static String status = null;
    public static boolean useResDetails = false;
    public static String loginId = null;
    public static String institution = null;
    public static String role = null;
    public static String det = null;
    public static String context = ConfigDefaults.CTX_DEVELOPMENT;
    
    /**
     *
     * @param args arg[0] operation, [1] gri for query, number requested for list
     * [2...] SubjectAttributes of requester
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static void main(String args[]) throws Exception {
        Logger log = null;
 
        parseArgs(args);

        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_COORD);
        cc.setContext(context);
        cc.setServiceName(ServiceNames.SVC_COORD);
        try {
            System.out.println("loading manifest from ./config/"+ConfigDefaults.MANIFEST);
            cc.loadManifest(ServiceNames.SVC_COORD,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();
            // need to do this after the log4j.properties file has been set
            log = Logger.getLogger(coordServerTest.class);
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }
        String configFile =cc.getFilePath(ConfigDefaults.CONFIG);
        Map config = ConfigHelper.getConfiguration(configFile);
        assert config != null : "No configuration";
        Map soap = (Map) config.get("soap");
        assert soap != null : "No soap stanza in configuration";
        String publishTo = (String) soap.get("publishTo");
        URL host = new URL(publishTo);
        URL wsdl = cc.getWSDLPath(null);
        System.out.println("host: " + host.toString() + " wsdl: " + wsdl.toString());
        CoordClient client = CoordClient.getClient (host, wsdl);

        if (request.equals("query"))
        {
            System.out.println("Invoking queryReservations...");
            GlobalReservationId GRI = new GlobalReservationId();
            GRI.setGri(gri);
            SubjectAttributes subjectAttrs = getSubjectAttrs(loginId,institution,role);
            Object[] req = new Object[]{subjectAttrs,GRI};
            try {
                Object[] res = client.invoke("queryReservation",req);
                ResDetails resDetails = (ResDetails)res[0];
                printResDetails(resDetails);
            } catch (OSCARSServiceException ex) {
                System.out.println("OSCARSServiceException thrown: " + ex.getMessage());
                ErrorReport errReport = ex.getErrorReport();
                if (errReport !=null) {
                    System.out.println(errReport.toString() );
                }
            }
        }
 
        if (request.equals("list"))
        {
            System.out.println("Invoking listReservations...");
            SubjectAttributes subjectAttrs = getSubjectAttrs(loginId,institution,role);
            ListRequest req = new ListRequest();
            req.setResOffset(0);
            System.out.println("Number requested is " + numReq);
            System.out.println("offset is " + offSet);
            try {
                req.setResRequested(numReq);
                req.setResOffset(offSet);
                Object[] soapReq = new Object[]{subjectAttrs,req};
                Object[] resp = client.invoke("listReservations", soapReq);

                ListReply reply = (ListReply) resp[0];
                System.out.println("total number of reservations is " + reply.getTotalResults());
                List<ResDetails> resDetailsList = reply.getResDetails();
                for (ResDetails resDetails: resDetailsList) {
                    printResDetails(resDetails);
                }
            } catch (OSCARSServiceException ex) {
                System.out.println("OSCARSServiceException thrown: " + ex.getMessage());
                ErrorReport errReport = ex.getErrorReport();
                if (errReport !=null) {
                    System.out.println(errReport.toString() );
                }
            }
        }
        if (request.equals("createPath")){
            System.out.println("Invoking createPath...");
            SubjectAttributes subjectAttrs = null;
            ResDetails resDetails = null;
            CreatePathContent createPath = null;
            subjectAttrs = getSubjectAttrs(loginId,institution,role);
            try {
                if (useResDetails ) {
                    GlobalReservationId GRI = new GlobalReservationId();
                    GRI.setGri(gri);
                    Object[] req = new Object[]{subjectAttrs,GRI};
                    Object[] res = client.invoke("queryReservation",req);
                    resDetails = (ResDetails)res[0];
                } else {
                    createPath = new CreatePathContent();
                    createPath.setGlobalReservationId(gri);
                }
                Object[] soapReq = new Object[]{subjectAttrs,createPath,resDetails};
                Object[] resp = client.invoke("createPath", soapReq);
            }
            catch (OSCARSServiceException ex) {
                System.out.println("OSCARSServiceException thrown: " + ex.getMessage());
               ErrorReport errReport = ex.getErrorReport();
                if (errReport !=null) {
                    System.out.println(errReport.toString() );
                }
            }
        }
        System.exit(0);
    }

    public static void parseArgs(String args[])  throws java.io.IOException {
        // create a parser
        OptionParser parser = new OptionParser();
        parser.acceptsAll( asList( "h", "?" ), "show help then exit" );
        OptionSpec<String> LOGIN = parser.accepts("u", "user login name").withRequiredArg().ofType(String.class);
        OptionSpec<String> INST = parser.accepts("i", "user's institution").withRequiredArg().ofType(String.class);
        OptionSpec<String> ROLE = parser.accepts("r", "user's role").withRequiredArg().ofType(String.class);
        OptionSpec<String> CMD = parser.accepts("c", "command:createPath,teardownPath,query,list,notify").withRequiredArg().ofType(String.class);
        OptionSpec<String> GRISPEC =  parser.accepts( "gri","globalReservationId" ).withRequiredArg().ofType( String.class );
        OptionSpec<Integer> NUM =parser.accepts( "n", "number of reservations requested").withRequiredArg().ofType( Integer.class );
        OptionSpec<Integer> OFFSET =parser.accepts( "o", "offset for reservations requested").withRequiredArg().ofType( Integer.class );
        OptionSpec<String> STATUS = parser.accepts("st", "status of reservations requested").withRequiredArg().ofType(String.class);
        OptionSpec<String> CONTEXT = parser.accepts("C", "context:INITTEST,DEVELOPMENT,SDK,PRODUCTION").withRequiredArg().ofType(String.class);
        OptionSpec<String> DET = parser.accepts("det", "use resDetails for call to pathCreate or Teardown").withRequiredArg().ofType(String.class);
        
        OptionSet options = parser.parse( args );

        // check for help
        if ( options.has( "?" ) || options.has("h")) {
            parser.printHelpOn( System.out );
            System.exit(0);
        }
        if (options.has(LOGIN)){
            loginId = options.valueOf(LOGIN);
        }
        if (options.has(INST)){
            institution = options.valueOf(INST);
        }
        if (options.has(ROLE)){
            role = options.valueOf(ROLE);
        }
        if (options.has (CMD)){
            request = options.valueOf(CMD);
        } else {
            parser.printHelpOn( System.out);
            System.exit(-1);
        }
        if (options.has(GRISPEC)) {
            gri=options.valueOf(GRISPEC);
        }
        if (options.has(NUM)){
            numReq = options.valueOf(NUM);
        }
        if (options.has(OFFSET)) {
            offSet = options.valueOf(OFFSET);
        }
        if (options.has(STATUS)){
            status = options.valueOf(STATUS);
        }
        if (options.has(DET)) {
            useResDetails = true;;
        }
        if (options.has(CONTEXT) ){
            context = options.valueOf(CONTEXT);
            if (!context.equals("UNITTEST") &&
                    !context.equals("SDK") &&
                    !context.equals("DEVELOPMENT") &&
                    !context.equals("PRODUCTION") ) 
            {
                System.out.println("unrecognized CONTEXT value: " + context);
                System.exit(-1);
            } 
        } 
        
        if (!request.equals("list")  && gri == null){
            System.out.println(" gri argument is required");
            System.exit(-1);
        }
        if (loginId ==  null || institution == null) {
            System.out.println("loginId and institution arguements are required");
            System.exit(-1);
        }
    }
    
    public static SubjectAttributes getSubjectAttrs(String loginName,String inst, String role){
        SubjectAttributes subjectAttrs = new SubjectAttributes();
        AttributeType loginId = new AttributeType();
        loginId.setName("loginId");
        loginId.getAttributeValue().add(loginName);
        subjectAttrs.getSubjectAttribute().add(loginId);
        AttributeType institution = new AttributeType();
        institution.setName("institution");
        institution.getAttributeValue().add(inst);
        subjectAttrs.getSubjectAttribute().add(institution);
       if (role != null ) {
            AttributeType attr = new AttributeType();
            attr.setName("role");
            attr.getAttributeValue().add(role);
            subjectAttrs.getSubjectAttribute().add(attr);
        }
        return subjectAttrs;
    }
    
    
    public static void printResDetails(ResDetails resDetails) {
        System.out.println("GRI: " + resDetails.getGlobalReservationId());
        System.out.println("Login: " + resDetails.getLogin());
        System.out.println("Status: "
                + resDetails.getStatus().toString());
        PathInfo pathInfo = null;
        String pathType = null;
        ReservedConstraintType rConstraint = resDetails.getReservedConstraint();
        if (rConstraint !=  null) {
            pathInfo=rConstraint.getPathInfo();
            pathType = "reserved";
        } else {
            UserRequestConstraintType uConstraint = resDetails.getUserRequestConstraint();
            if (uConstraint ==null) {
                System.out.println("invalid reservation, no reserved or requested path");
                return;
            }
            pathInfo=uConstraint.getPathInfo();
            pathType = "requested";
            System.out.println("no path reserved, using requested path ");
        }
        Layer3Info layer3Info = pathInfo.getLayer3Info();
        if (layer3Info != null) {
            System.out.println("Source host: " +
                    layer3Info.getSrcHost());
            System.out.println("Destination host: " +
                    layer3Info.getDestHost());
        } else {
            Layer2Info layer2Info = pathInfo.getLayer2Info();
            if (layer2Info != null) {
                System.out.println("Source urn: " +
                        layer2Info.getSrcEndpoint());
                System.out.println("Destination urn: " +
                        layer2Info.getDestEndpoint());
            }
        }
        System.out.println(" ");
        System.out.println(" ");
        CtrlPlanePathContent path = pathInfo.getPath();
        if (path != null) {
            List<CtrlPlaneHopContent> hops = path.getHop();
            if (hops.size() > 0) {
                System.out.println("Hops in " + pathType + " path are:");
                for ( CtrlPlaneHopContent ctrlHop : hops ) {
                    CtrlPlaneLinkContent link = ctrlHop.getLink();
                    if (link != null ) {
                        System.out.println(link.getId());
                    } else {
                        String id = ctrlHop.getLinkIdRef();
                        System.out.println(id);
                    }
                }
            }
        } else {
            System.out.println("no path in " + pathType + " constraint");
        }
    }
}
