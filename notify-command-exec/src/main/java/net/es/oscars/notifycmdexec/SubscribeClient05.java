package net.es.oscars.notifycmdexec;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.cxf.frontend.ClientProxy;
import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;

import net.es.oscars.api.soap.gen.v05.OSCARSNotify;
import net.es.oscars.api.soap.gen.v05.OSCARSNotify_Service;
import net.es.oscars.client.Client;
import net.es.oscars.client.OSCARSClientConfig;
import net.es.oscars.client.OSCARSClientException;

public class SubscribeClient05 extends Client<OSCARSNotify>{
    
    final private String NAMESPACE = "http://oscars.es.net/OSCARS";
    final private String SERVICE_NAME = "OSCARSNotify";
    
    public SubscribeClient05(String serviceUrl, String wsdlUrl) throws OSCARSClientException{
        //Create PortType
        URL wsdlUrlObj = null;
        try {
            wsdlUrlObj = new URL(wsdlUrl);
        } catch (MalformedURLException e) {
            throw new OSCARSClientException("Malformed URL " + wsdlUrl);
        }
        //init ssl so we can grab the wsdl
        if(OSCARSClientConfig.getSSLKeystoreFile() != null){
            System.setProperty("javax.net.ssl.trustStore", OSCARSClientConfig.getSSLKeystoreFile());
        }
        if(OSCARSClientConfig.getSSLKeystorePassword() != null){
            System.setProperty("javax.net.ssl.trustStorePassword", OSCARSClientConfig.getSSLKeystorePassword());
        }
        OSCARSNotify_Service service = new OSCARSNotify_Service(wsdlUrlObj, new QName (NAMESPACE, SERVICE_NAME));
        this.portType = (OSCARSNotify) service.getPort(OSCARSNotify.class);
        if(serviceUrl != null){
            ClientProxy.getClient(this.portType).getRequestContext().put("org.apache.cxf.message.Message.ENDPOINT_ADDRESS", serviceUrl);
        }
    }
    
    public SubscribeClient05(String serviceUrl) throws OSCARSClientException{
        this(serviceUrl, serviceUrl + "?wsdl");
    }
    
    public SubscribeResponse subscribe(Subscribe request) throws OSCARSClientException{
        this.prepareClient();
        try{
            return this.portType.subscribe(request);
        }catch(Exception e){
            e.printStackTrace();
            throw new OSCARSClientException(e.getMessage());
            
        }
    }
    
    public RenewResponse renew(Renew request) throws OSCARSClientException{
        this.prepareClient();
        try{
            return this.portType.renew(request);
        }catch(Exception e){
            throw new OSCARSClientException(e.getMessage());
        }
    }
    
    public UnsubscribeResponse unsubscribe(Unsubscribe request) throws OSCARSClientException{
        this.prepareClient();
        try{
            return this.portType.unsubscribe(request);
        }catch(Exception e){
            throw new OSCARSClientException(e.getMessage());
        }
    }
}
