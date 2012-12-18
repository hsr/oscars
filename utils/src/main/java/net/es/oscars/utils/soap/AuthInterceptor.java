package net.es.oscars.utils.soap;

import java.util.Map;
import java.util.Vector;
import java.util.Iterator;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPElement;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AuthInterceptor extends WSS4JInInterceptor {
    private SAAJInInterceptor saaj = new SAAJInInterceptor();
    
    public AuthInterceptor(){
        super();
    }
    public AuthInterceptor(Map<String,Object> properties){
        super(properties);
    }

    private SOAPMessage getSOAPMsg (SoapMessage msg) {
        SOAPMessage doc = msg.getContent(SOAPMessage.class);
        if (doc == null) {
            saaj.handleMessage(msg);
            doc = msg.getContent(SOAPMessage.class);
        }
        return doc;
    }



    @Override
    public void handleMessage(SoapMessage message) throws Fault {

        try {
            
            boolean hasWSS4JHeader  = false;
            boolean hasUserPassword = false;
            boolean hasX509Header   = false;

            try {
                // Check if there is a WSS4J (user/password) in message
                // TODO: if there is not WSS4J header, then verify that the message is signed
                SOAPMessage msg = getSOAPMsg (message);
                msg.writeTo (System.out);
                Iterator<SOAPElement> elements = msg.getSOAPHeader().getChildElements();
                while (elements.hasNext()) {
                    SOAPElement el = (SOAPElement) elements.next();
                    String localName = el.getLocalName();
                    if ("Security".equals(localName)) {
                        hasWSS4JHeader = true;
                    }
                }
                 
                if (hasWSS4JHeader) {
                    
                    NodeList headerNodes = msg.getSOAPHeader().getChildNodes();
                    for (int i=0; i < headerNodes.getLength(); ++i) {
                        Node node = headerNodes.item(i);
                        if ("Security".equals(node.getLocalName())) {
                            NodeList securityNodes = node.getChildNodes();
                            for (int j=0; j < securityNodes.getLength(); ++j) {
                                Node securityNode = securityNodes.item(j);
                                String securityNodeName = securityNode.getLocalName();
                                if ("Signature".equals(securityNodeName)) {
                                    hasX509Header = true;
                                } else if ("UsernameToken".equals(securityNodeName)) {
                                    hasUserPassword = true;
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException (ex);
            }
            
            if (hasWSS4JHeader) {
                if (hasX509Header) {
                    super.handleMessage (message);
                } else if (hasUserPassword) {
                    super.handleMessage(message);
                    Vector<WSHandlerResult> result = (Vector<WSHandlerResult>) message.getContextualProperty(WSHandlerConstants.RECV_RESULTS);
                    if (result != null && !result.isEmpty()) {
                        for (WSHandlerResult res : result) {
                            // loop through security engine results
                            for (WSSecurityEngineResult securityResult : (Vector<WSSecurityEngineResult>) res.getResults()) {
                                int action = (Integer) securityResult.get(WSSecurityEngineResult.TAG_ACTION);
                                // determine if the action was a username token
                                if ((action & WSConstants.UT) > 0) {
                                    // get the principal object
                                    WSUsernameTokenPrincipal principal = (WSUsernameTokenPrincipal) securityResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
                                    if (principal.getPassword()==null){
                                        principal.setPassword("");
                                    }
                                }
                            }
                        }
                    }                   
                }
            } else {
                throw new RuntimeException ("Client is using invalid auth.");
            }
            

        } catch (RuntimeException ex) {
            throw ex;
        }
    }


}