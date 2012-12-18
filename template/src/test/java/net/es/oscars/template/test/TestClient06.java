
package net.es.oscars.template.test;

import java.net.URL;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.io.IOException;
import java.io.FileInputStream;
import org.apache.cxf.configuration.security.FiltersType;

import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.config.SharedConfig;

import net.es.oscars.template.soap.gen.v06.*;

@OSCARSService (
		implementor = "net.es.oscars.template.soap.gen.v06.TemplateService",
		namespace = "http://oscars.es.net/OSCARS/template/06",
		serviceName = "TemplateService",
		config="config.yaml"
)
public final class TestClient06 extends OSCARSSoapService<TemplateService,TemplatePortType> {

    private TestClient06(URL host, URL wsdlFile) throws OSCARSServiceException {
    	super (host, wsdlFile, TemplatePortType.class);
    }
 
    public static void main(String args[]) {

    	try {

	        OSCARSSoapService.setSSLBusConfiguration((
	                new URL("file:" + (new SharedConfig ("TemplateService")).getFilePath("client-cxf.xml"))));
	        
    	   
        	TestClient06 client = new TestClient06 (new URL(args[0]), new URL(args[1]));

        	TemplatePortType port = client.getPortType();
        	Client c = ClientProxy.getClient(port);
        	
        	
/**
 * The following code is used, but useful for debug purposes. Leaving it commented out for as sample
 */

/**
        	HTTPConduit conduit = (HTTPConduit) c.getConduit();
        	
        	String keystore = "/Users/lomax/var/oscars/sampleDomain/certs/client.jks";
        	KeyManager[] kmgrs = getKeyManagers(getKeyStore("JKS", keystore, "changeit"), "changeit");
 
        	String truststore = "/Users/lomax/var/oscars/sampleDomain/certs/truststore.jks";
            TrustManager[] tmgrs = getTrustManagers(getKeyStore("JKS", truststore, "changeit"));

            TLSClientParameters tlsClientParameters = new TLSClientParameters();
            
            tlsClientParameters.setKeyManagers(kmgrs);
            tlsClientParameters.setTrustManagers(tmgrs);
            FiltersType filters = new FiltersType();
            filters.getInclude().add(".*_EXPORT_.*");
            filters.getInclude().add(".*_EXPORT1024_.*");
            filters.getInclude().add(".*_WITH_DES_.*");
            filters.getInclude().add(".*_WITH_NULL_.*");
            filters.getInclude().add(".*_DH_anon_.*");
            tlsClientParameters.setCipherSuitesFilter(filters);
            conduit.setTlsClientParameters(tlsClientParameters);
            System.out.println ("############################>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + conduit.getBeanName());

**/
        	
        	
            RequestType request = new RequestType();
            request.setPrefix("PrefixInput");
            request.setInputData("DataInput");

    		Object[] req = new Object[]{request};
    		Object[] res = client.invoke("Query",req);
 
    		System.out.println("Response: " + (String) res[0]);
    		
    	} catch (Exception ee) {
    		System.out.println ("Exception" + ee);
    	}

        System.exit(0);
    }
    
    public static KeyStore getKeyStore(String ksType, String file, String ksPassword)
    throws GeneralSecurityException,
           IOException {
    
    String type = ksType != null
                ? ksType
                : KeyStore.getDefaultType();
                
    char[] password = ksPassword != null
                ? ksPassword.toCharArray()
                : null;

    // We just use the default Keystore provider
    KeyStore keyStore = KeyStore.getInstance(type);
    
    keyStore.load(new FileInputStream(file), password);
    
    return keyStore;
}
    
    public static KeyManager[] getKeyManagers(KeyStore keyStore, String keyPassword) 
    throws GeneralSecurityException,
           IOException {
    // For tests, we just use the default algorithm
    String alg = KeyManagerFactory.getDefaultAlgorithm();
    
    char[] keyPass = keyPassword != null
                 ? keyPassword.toCharArray()
                 : null;
    
    // For tests, we just use the default provider.
    KeyManagerFactory fac = KeyManagerFactory.getInstance(alg);
                 
    fac.init(keyStore, keyPass);
    
    return fac.getKeyManagers();
}

public static TrustManager[] getTrustManagers(KeyStore keyStore) 
    throws GeneralSecurityException,
           IOException {
    // For tests, we just use the default algorithm
    String alg = TrustManagerFactory.getDefaultAlgorithm();
    
    // For tests, we just use the default provider.
    TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
                 
    fac.init(keyStore);
    
    return fac.getTrustManagers();
}

}
