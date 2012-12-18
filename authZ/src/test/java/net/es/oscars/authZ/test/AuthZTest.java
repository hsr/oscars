package net.es.oscars.authZ.test;

import static java.util.Arrays.asList;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.clients.AuthZClient;
import net.es.oscars.authZ.soap.gen.CheckAccessParams;
import net.es.oscars.authZ.soap.gen.CheckAccessReply;
import net.es.oscars.authZ.soap.gen.CheckMultiAccessParams;
import net.es.oscars.authZ.soap.gen.MultiAccessPerm;
import net.es.oscars.authZ.soap.gen.MultiAccessPerms;
import net.es.oscars.authZ.soap.gen.PermType;
import net.es.oscars.authZ.soap.gen.ReqPermType;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.common.soap.gen.AuthConditionType;
import net.es.oscars.common.soap.gen.OSCARSFault;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

public final class AuthZTest {

    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_AUTHZ);
    private static String context = ConfigDefaults.CTX_DEVELOPMENT;
    private static OptionParser parser = new OptionParser();
    private static String cmd = null;
    private static String resource =  null;
    private static String requestedAction = null;
    private static List<String> roles = null;


    /**
     *
     * @param args arg[0] operation, [1] resource  [2..] list of attribute values
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {

        parseArgs(args);

        cc.setContext(context);
        cc.setServiceName(ServiceNames.SVC_AUTHZ);
        try {
            System.out.println("loading manifest from ./config/"+ConfigDefaults.MANIFEST);
            cc.loadManifest(ServiceNames.SVC_AUTHZ,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }
        
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(ModuleName.AUTHZ,"0000");
        String configFile =cc.getFilePath(ConfigDefaults.CONFIG);
        Map config = ConfigHelper.getConfiguration(configFile);
        assert config != null : "No configuration";
        Map authN = (Map) config.get("soap");
        URL host = new URL((String)authN.get("publishTo"));
        URL wsdl = cc.getWSDLPath(null);
        System.out.println("host is " + host.toString() + "wsdl is " + wsdl.toString());

        AuthZClient client = AuthZClient.getClient (host, wsdl);
 
        /* assumes requested permission is "list" */
        if (cmd.equals("checkAccess"))
        {
            System.out.println("Invoking checkAccess...");
            CheckAccessParams params = new CheckAccessParams();
            SubjectAttributes subAttrs = new SubjectAttributes();
            List<AttributeType> reqAttrs = subAttrs.getSubjectAttribute();
            for ( String role : roles) {
                AttributeType at = new AttributeType();
                at.setName("role"); // e.g. role, privilege ...
                at.getAttributeValue().add(role);// e.g. OSCARS_Engineer, setPathElements
                reqAttrs.add(at);
            }
            // add loginId and institution attributes 
            AttributeType at = new AttributeType();
            at.setName(AuthZConstants.LOGIN_ID);
            at.getAttributeValue().add("client");
            reqAttrs.add(at);
            at = new AttributeType();
            at.setName(AuthZConstants.INSTITUTION);
            at.getAttributeValue().add("ESNet");
            reqAttrs.add(at);
            params.setSubjectAttrs(subAttrs);
            params.setResourceName(resource);
            params.setPermissionName(requestedAction);
            Object[] req = new Object[]{params};
            try {
                Object[] res = client.invoke("checkAccess",req);
                CheckAccessReply reply = (CheckAccessReply)res[0];
                AuthConditions authConds = reply.getConditions();
                System.out.println("access is " + reply.getPermission());
                for (AuthConditionType authCond : authConds.getAuthCondition()) {
                    System.out.println (authCond.getName() + ": " + authCond.getConditionValue());
                }
            } catch (OSCARSServiceException ex) {
                System.out.println("OSCARSServiceException thrown: " + ex.getMessage());
                ErrorReport errReport = ex.getErrorReport();
                if (errReport !=null) {
                    System.out.println(errReport.toString() );
                }
            }
        }
        /* assumes requested permissions are: "list", "query" and "create" */
        if (cmd.equals("checkMultiAccess"))
        {
            System.out.println("Invoking checkMultiAccess...");
            CheckMultiAccessParams req = new CheckMultiAccessParams();
            try {

                List<ReqPermType> reqPermTypes = req.getReqPermissions();
                SubjectAttributes subAttrs = req.getSubjectAttrs();
                List<AttributeType> reqAttrs = subAttrs.getSubjectAttribute();
                for ( String role: roles) {
                    AttributeType at = new AttributeType();
                    at.setName("role"); // e.g. role, privilege ...
                    at.getAttributeValue().add(role);// e.g. OSCARS_Engineer, setPathElements
                    reqAttrs.add(at);
                }
                req.setSubjectAttrs(subAttrs);
                // this part doesn't work
                ReqPermType rp = new ReqPermType();
                rp.setResource(resource);
                List<String> permissions = rp.getReqAction();
                permissions.add("list");
                permissions.add("query");
                permissions.add("create");
                reqPermTypes.add(rp);
                Object[] soapReq = new Object[]{req};
                System.out.println("to client invoke");
                Object[] resp = client.invoke("checkMultiAccess", soapReq);
                System.out.println("past client invoke");
                MultiAccessPerms reply = (MultiAccessPerms) resp[0];
                List<MultiAccessPerm> accessPerms = reply.getAccessPerm();
                Map<String, HashMap<String, String>> resourcePerms =
                    new HashMap<String, HashMap<String,String>>();
                for (MultiAccessPerm accessPerm: accessPerms) {
                    HashMap<String, String> permMap = new HashMap<String, String>();
                    System.out.println("resource: " + accessPerm.getResource());
                    List<PermType> permTypes = accessPerm.getPermissionGranted();
                    for (PermType permType: permTypes) {
                        System.out.println("permission: " + permType.getPermission());
                        System.out.println("auth: " + permType.getAction());
                        permMap.put(permType.getPermission(), permType.getAction());
                    }
                    resourcePerms.put(accessPerm.getResource(), permMap);
                }

            } catch (OSCARSServiceException ex) {
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

        parser.acceptsAll( asList( "h", "?" ), "show help then exit" );
        OptionSpec<String> CMD = parser.accepts("c", "command").withRequiredArg().ofType(String.class);
        OptionSpec<String> RESOURCE = parser.accepts("r", "resource").withRequiredArg().ofType(String.class);
        OptionSpec<String> PERMISSION = parser.accepts("p", "permission").withRequiredArg().ofType(String.class);
        OptionSpec<String> ATTR = parser.accepts("a", "attribute").withRequiredArg().ofType(String.class);
        OptionSpec<String> CONTEXT = parser.accepts("C", "context:UNITTEST,DEVELOPMENT,SDK,PRODUCTION").withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse( args );

        // check for help
        if ( options.has( "?" ) ) {
            parser.printHelpOn( System.out );
            System.exit(0);
        }
        if (options.has(CMD)) {
            cmd = options.valueOf(CMD);
        } else {
            System.out.println("a -c cmd argument is required");
            parser.printHelpOn(System.out);
            System.exit(-1);
        }
        if (options.has(RESOURCE)) {
            resource = options.valueOf(RESOURCE);
        } else {
            parser.printHelpOn(System.out);
            System.exit(-1);
        }
        if (options.has(PERMISSION)){
            requestedAction = options.valueOf(PERMISSION);
        } else {
            parser.printHelpOn(System.out);
            System.exit(-1);
        }
        if (options.has(ATTR)) {
            roles = options.valuesOf(ATTR);
        } else {
            parser.printHelpOn(System.out);
            System.exit(-1);
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
    }
}

