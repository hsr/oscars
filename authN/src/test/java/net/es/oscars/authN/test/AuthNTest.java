package net.es.oscars.authN.test;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Map;
import java.net.URL;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import net.es.oscars.utils.soap.ErrorReport;
import org.apache.log4j.Logger;

import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.authN.soap.gen.*;
import net.es.oscars.common.soap.gen.OSCARSFault;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.clients.AuthNClient;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

/**
 * AuthNTest - class to test calls to the AuthN service
 *
 */

public final class AuthNTest {

    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_AUTHN);
    private static String cmd = null;
    private static String context = ConfigDefaults.CTX_DEVELOPMENT;
    private static String loginName =  null;
    private static String password = null;
    private static String issuer = null;
    private static String subject = null;
    private static OptionParser parser = new OptionParser();

     /**
      *
      * @param args arg[0] -c cmd, [1] -u loginName or -s Subject DN, [2] -p password or -i Issuer DN
      *     [3] -C context
      * @throws Exception
      */
    public static void main(String args[]) throws Exception {

        parseArgs(args);

        cc.setContext(context);
        cc.setServiceName(ServiceNames.SVC_AUTHN);
        try {
            cc.loadManifest(ServiceNames.SVC_AUTHN,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }

        String configFile =cc.getFilePath(ConfigDefaults.CONFIG);
        Map config = ConfigHelper.getConfiguration(configFile);
        assert config != null : "No configuration";
        Map authN = (Map) config.get("soap");
        URL host = new URL((String)authN.get("publishTo"));
        URL wsdl = cc.getWSDLPath(null);
        System.out.println("host is " + host.toString() + "wsdl is " + wsdl.toString());
        AuthNClient client = AuthNClient.getClient (host, wsdl);
        
        
        if (cmd.equals("verifyLogin"))
        {
            System.out.println("Invoking verifyLogin...");
            if (loginName == null || password == null) {
                parser.printHelpOn( System.out );
                System.exit(0);
            }
            LoginId loginId = new LoginId();
            VerifyLoginReqType verifyLoginReq = new VerifyLoginReqType();
            loginId.setLoginName(loginName);
            loginId.setPassword(password);
            verifyLoginReq.setLoginId(loginId);
            verifyLoginReq.setTransactionId("0000");
            Object[] req = new Object[]{verifyLoginReq};

            try {
                Object[] res = client.invoke("verifyLogin",req);
                VerifyReply reply = (VerifyReply)res[0];
                SubjectAttributes subjectAttrs = reply.getSubjectAttributes();
                List<AttributeType> attrs = subjectAttrs.getSubjectAttribute();
                if ( attrs.isEmpty()) {
                    System.out.println("verifyLogin result= " + loginName + " has no attributes");
                } else {
                    for (AttributeType at : attrs) {
                        System.out.println("verifyLogin.result=" + at.getName() + " : " +
                                at.getAttributeValue());
                    }
                }
            } catch (OSCARSServiceException ex) {
                System.out.println("OSCARSServiceException thrown; " + ex.getMessage());
                ErrorReport errReport = ex.getErrorReport();
                if (errReport !=null) {
                    System.out.println(errReport.toString() );
                }
            }
        }
        else if (cmd.equals("verifyDN"))
        {
            System.out.println("Invoking verifyDN...");
            if (issuer == null || subject == null){
                parser.printHelpOn( System.out );
                System.exit(0);
            }
            VerifyDNReqType verifyDNReq = new VerifyDNReqType();
            DNType DNReq = new DNType();
            DNReq.setSubjectDN(subject);
            DNReq.setIssuerDN(issuer);
            verifyDNReq.setTransactionId("123");
            verifyDNReq.setDN(DNReq);
            Object[] req = new Object[]{verifyDNReq};
            try {
               Object[] res = client.invoke("verifyDN",req);
               VerifyReply reply = (VerifyReply)res[0];
               SubjectAttributes subjectAttrs = reply.getSubjectAttributes();
               List<AttributeType> attrs = subjectAttrs.getSubjectAttribute();
               if ( attrs.isEmpty()) {
                   System.out.println("verifyDN result= user has no attributes");
               } else {
                   for (AttributeType at : attrs) {
                       System.out.println("verifyDN.result=" + at.getName() + " : " +
                               at.getAttributeValue());
                   }
               }
            } catch (OSCARSServiceException ex) {
                System.out.println("OSCARSServiceException thrown: " + ex.getMessage());
                ErrorReport errReport = ex.getErrorReport();
                if (errReport !=null) {
                    System.out.println(errReport.toString() );
                }
            }
        } else {
            System.out.println("unrecognized command: " + cmd );
        }

        System.exit(0);
    }
    public static void parseArgs(String args[])  throws java.io.IOException {

        parser.acceptsAll( asList( "h", "?" ), "show help then exit" );
        OptionSpec<String> CMD = parser.accepts("c", "command").withRequiredArg().ofType(String.class);
        OptionSpec<String> LOGIN = parser.accepts("u", "LoginId").withRequiredArg().ofType(String.class);
        OptionSpec<String> PASSWORD = parser.accepts("p", "password").withRequiredArg().ofType(String.class);
        OptionSpec<String> ISSUER = parser.accepts("i", "IssuerDN").withRequiredArg().ofType(String.class);
        OptionSpec<String> SUBJECT = parser.accepts("s", "SubjectDN").withRequiredArg().ofType(String.class);       
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
            System.exit(0);
        }
        if (options.has(LOGIN)) {
            loginName = options.valueOf(LOGIN);
        }
        if (options.has(PASSWORD)){
            password = options.valueOf(PASSWORD);
        }
        if (options.has(ISSUER)) {
            issuer = options.valueOf(ISSUER);
        }
        if (options.has(SUBJECT)){
            subject = options.valueOf(SUBJECT);
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
