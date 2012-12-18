package net.es.oscars.nsibridge.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;


import net.es.oscars.nsibridge.beans.config.JettyConfig;
import net.es.oscars.nsibridge.beans.config.JettyServiceConfig;

import net.es.oscars.utils.config.ConfigHelper;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.security.ClientAuthentication;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngineFactory;
import org.apache.log4j.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class JettyContainer {
    private String hostname;

    public HashMap<String, Object> getSoapHandlers() {
        return soapHandlers;
    }

    private HashMap<String, Object> soapHandlers = new HashMap<String, Object>();

    private JettyContainer() {} ;
    private static JettyContainer instance;
    public static JettyContainer getInstance() {
        if (instance == null) instance = new JettyContainer();
        return instance;
    }


    private JettyConfig config;

    private Logger log = Logger.getLogger(JettyContainer.class);



    public void startServer() {

        String hostname = config.getHttp().getHostname();
        Integer port = config.getHttp().getPort();

        boolean useSSL = config.getSsl().isUseSSL();
        boolean useBasicAuth = config.getAuth().isUseBasicAuth();


        String basePath = "http://"+hostname+":"+port+"/";
        if (useSSL) {
            basePath = "https://"+hostname+":"+port+"/";
        }
        log.info("Starting jetty at: "+basePath);

        JettyServiceConfig[] serviceConfigs = config.getServices();

        for (JettyServiceConfig serviceConfig: serviceConfigs) {
            JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();

            String servicePath = serviceConfig.getPath();
            String implementor = serviceConfig.getImplementor();
            try {
                Object implementorObj = Class.forName(implementor).getConstructor((Class[]) null).newInstance((Object[]) null);
                soapHandlers.put(servicePath, implementorObj);

                sf.setAddress(basePath + servicePath);
                sf.setServiceClass(implementorObj.getClass());
                sf.getServiceFactory().setInvoker(new BeanInvoker(implementorObj));
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Problem loading class: "+implementor);
                System.exit(1);
            }


            if (useSSL) {
                log.info("Using SSL");
                sf = configureSSLOnTheServer(sf, port);
            }

            org.apache.cxf.endpoint.Server server = sf.create();
            if (useBasicAuth) {
                String passwdFile = config.getAuth().getPasswdFileName();
                log.info("Using HTTP Basic Auth with passwords from "+passwdFile);
                BasicAuthAuthorizationInterceptor icpt = new BasicAuthAuthorizationInterceptor();
                HashMap <String, String> passwds = new HashMap <String, String>();
                passwds = ConfigHelper.getConfiguration(passwdFile, passwds.getClass());
                icpt.setUsers(passwds);
                server.getEndpoint().getInInterceptors().add(icpt);
            }


            /*
            for (String inInterceptor : serviceConfig.getInInterceptors()) {
                try {
                    Interceptor interceptorObj = (Interceptor) Class.forName(inInterceptor).getConstructor((Class[]) null).newInstance((Object[]) null);
                    server.getEndpoint().getInInterceptors().add(interceptorObj);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Problem loading class: "+inInterceptor);
                    System.exit(1);
                }
            }
            */

            String endpoint = server.getEndpoint().getEndpointInfo().getAddress();
            log.info("Server started at " + endpoint);
        }


    }



    private JaxWsServerFactoryBean configureSSLOnTheServer(JaxWsServerFactoryBean sf, int port) {
        String sslKeystorePath      = config.getSsl().getSslKeystorePath();
        String sslKeystorePass      = config.getSsl().getSslKeystorePass();
        String sslKeyPass           = config.getSsl().getSslKeyPass();
        String sslTruststorePath    = config.getSsl().getSslKeystorePath();
        String sslTruststorePass    = config.getSsl().getSslTruststorePass();

        try {

            TLSServerParameters tlsParams = new TLSServerParameters();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            File keystoreFile = new File(sslKeystorePath);
            keyStore.load(new FileInputStream(keystoreFile), sslKeystorePass.toCharArray());
            KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyFactory.init(keyStore, sslKeyPass.toCharArray());
            KeyManager[] km = keyFactory.getKeyManagers();
             tlsParams.setKeyManagers(km);

            File truststoreFile = new File(sslTruststorePath);
            keyStore.load(new FileInputStream(truststoreFile), sslTruststorePass.toCharArray());
            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(keyStore);
            TrustManager[] tm = trustFactory.getTrustManagers();
            tlsParams.setTrustManagers(tm);
            FiltersType filter = new FiltersType();
            filter.getInclude().add(".*_EXPORT_.*");
            filter.getInclude().add(".*_EXPORT1024_.*");
            filter.getInclude().add(".*_WITH_DES_.*");
            filter.getInclude().add(".*_WITH_NULL_.*");
            filter.getExclude().add(".*_DH_anon_.*");
            tlsParams.setCipherSuitesFilter(filter);
            ClientAuthentication ca = new ClientAuthentication();
            ca.setRequired(false);
            ca.setWant(false);
            tlsParams.setClientAuthentication(ca);
            JettyHTTPServerEngineFactory factory = new JettyHTTPServerEngineFactory();
            factory.setTLSServerParametersForPort(port, tlsParams);
        } catch (KeyStoreException kse) {
            kse.printStackTrace();
            log.error(kse);
            log.error("Security configuration failed with the following: " + kse.getCause());
        } catch (NoSuchAlgorithmException nsa) {
            nsa.printStackTrace();
            log.error(nsa);
            log.error("Security configuration failed with the following: " + nsa.getCause());
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            log.error(fnfe);
            log.error("Security configuration failed with the following: " + fnfe.getCause());
        } catch (UnrecoverableKeyException uke) {
            uke.printStackTrace();
            log.error(uke);
            log.error("Security configuration failed with the following: " + uke.getCause());
        } catch (CertificateException ce) {
            ce.printStackTrace();
            log.error(ce);
            log.error("Security configuration failed with the following: " + ce.getCause());
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            log.error(gse);
            log.error("Security configuration failed with the following: " + gse.getCause());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            log.error(ioe);
            log.error("Security configuration failed with the following: " + ioe.getCause());
        }

        return sf;

    }

    public JettyConfig getConfig() {
        return config;
    }

    public void setConfig(JettyConfig config) {
        this.config = config;
    }
}
