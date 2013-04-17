package edu.internet2.ion.ionui.http;

import java.util.Map;
import java.io.File;
import java.io.FileInputStream;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.xml.XmlConfiguration;

import net.es.oscars.utils.config.*;
import net.es.oscars.utils.svc.ServiceNames;

public class WebApp {
    public static ContextConfig cc = null;

    public static void main(String[] args) throws Exception {

        cc = ContextConfig.getInstance(ServiceNames.SVC_IONUI);
        cc.setServiceName(ServiceNames.SVC_IONUI);
        String context = "PRODUCTION";
	String contextPath="ion";

        if (args.length > 0) {
            context = args[1];
        }
        String warFile = null;
        String warTempDir = null;
        Server server = new Server();

        cc.setContext(context);
        System.setProperty("context",context);
        System.out.println("starting IONUI with context "+ context);
        try {
            cc.loadManifest(ServiceNames.SVC_IONUI,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();

            String configFile = cc.getFilePath(ConfigDefaults.CONFIG);
            Map config = ConfigHelper.getConfiguration(configFile);
            Map http = (Map) config.get("http");
            warFile = (String) http.get("warFile");
            warTempDir = (String) http.get("warTempDir");
            String jettyConf = cc.getFilePath("jetty.xml");
            XmlConfiguration configuration = new XmlConfiguration(new FileInputStream(jettyConf));
            configuration.configure(server);
System.out.println(" configuration = configuration" );
System.out.println(" from jetty conf="+ jettyConf);
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }
        WebAppContext webapp = new WebAppContext();
	String sTempContext = java.io.File.separatorChar + contextPath;
	System.out.println("Context ="+ sTempContext);
        webapp.setContextPath(sTempContext);
	//webapp.setContextPath("/ion");
        webapp.setWar(warFile);
        if(warTempDir != null && !warTempDir.equals("")){
        	webapp.setTempDirectory(new File(warTempDir));
        }
        server.setHandler(webapp);
        server.start();
        server.join();
    }
}
