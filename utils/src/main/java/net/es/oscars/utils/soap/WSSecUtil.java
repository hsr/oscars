package net.es.oscars.utils.soap;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.ws.handler.MessageContext;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;

import org.apache.log4j.Logger;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import javax.xml.ws.WebServiceContext;

public class WSSecUtil {
    private static Logger LOG = Logger.getLogger(WSSecUtil.class);
    
    static public HashMap<String, Principal> getSecurityPrincipals(WebServiceContext context) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "getSecurityPrincipals";
        LOG.debug(netLogger.start(event));
        HashMap<String, Principal> result = new HashMap<String, Principal>();

        try {
            MessageContext inContext = (MessageContext) context.getMessageContext();
            if (inContext == null) {
                LOG.error(netLogger.error(event,ErrSev.MAJOR, "message context is NULL"));
                return null;
            }
            Vector results = (Vector) inContext.get(WSHandlerConstants.RECV_RESULTS);
            
            for (int i = 0; results != null && i < results.size(); i++) {
                WSHandlerResult hResult = (WSHandlerResult) results.get(i);
                Vector hResults = hResult.getResults();
                for (int j = 0; j < hResults.size(); j++) {
                    WSSecurityEngineResult eResult = (WSSecurityEngineResult) hResults.get(j);
                    // A timestamp action does not have an
                    // associated principal. Only Signature and UsernameToken
                    // actions return a principal.
                    if ((((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.SIGN)) {
                        Principal subjectDN = ((X509Certificate) eResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE)).getSubjectDN();
                        Principal issuerDN = ((X509Certificate) eResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE)).getIssuerDN();
                        result.put("subject", subjectDN);
                        result.put("issuer", issuerDN);
                    }
                    else if ((((java.lang.Integer) eResult.get(
                                WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.UT)) {
                        Principal subjectName = (Principal) eResult.get(
                                WSSecurityEngineResult.TAG_PRINCIPAL);
                        result.put("userTokenName", subjectName);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(netLogger.error(event,ErrSev.MAJOR,
                                    "caught.exception: " + e.toString()));
            e.printStackTrace();
            return null;
        }
        LOG.debug(netLogger.end(event));
        return result;
    }
}
