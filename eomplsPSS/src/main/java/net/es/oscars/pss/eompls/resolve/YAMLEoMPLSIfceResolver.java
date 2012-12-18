package net.es.oscars.pss.eompls.resolve;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.ho.yaml.Yaml;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

public class YAMLEoMPLSIfceResolver implements EoMPLSIfceAddressResolver {
    private Logger log = Logger.getLogger(YAMLEoMPLSIfceResolver.class);
    private GenericConfig config = null;
    private HashMap<URNParserResult, String> ifceAddresses = null;
    private String yamlFilename = null;
    private Long lastUpdated = null;


    public YAMLEoMPLSIfceResolver() {
    }
    
    public String getIfceAddress(String ifceId) throws PSSException {
        if (this.config == null) {
            throw new PSSException("null config");
        }
        if (ifceId == null ) {
            throw new PSSException("null ifceId");
        }
        this.checkIfConfigUpdated();

        URNParserResult urn = URNParser.parseTopoIdent(ifceId);
        System.out.println("resolving: "+urn.toString());

        if (ifceAddresses == null) {
            throw new PSSException("empty config for ifceAddresses");
        } else {
            String address = ifceAddresses.get(urn);
            if (address == null) {
                throw new PSSException("address not found for ifce: "+ifceId);
            } else {
                return address;
            }
        }
    }

    private void checkIfConfigUpdated() throws PSSException {
        File yamlFile = new File(yamlFilename);
        if (lastUpdated == null || yamlFile.lastModified() > lastUpdated) {
            this.loadYaml();
        }
    }

    private void loadYaml() throws PSSException {
        ifceAddresses = new HashMap<URNParserResult, String>();
        try {
            InputStream propFile =  new FileInputStream(new File(yamlFilename));

            HashMap<String, String> configAddresses = (HashMap<String, String>) Yaml.load(propFile);
            for (String ifceId : configAddresses.keySet()) {
                String address = configAddresses.get(ifceId);
                URNParserResult urn = URNParser.parseTopoIdent(ifceId);
                log.debug("putting address: " + address + " for urn " + urn);
                ifceAddresses.put(urn, address);
            }
        } catch (FileNotFoundException e) {
            throw new PSSException(e);
        }
        lastUpdated = new Date().getTime();
    }


    @SuppressWarnings({ "unchecked" })
    public void setConfig(GenericConfig config) throws PSSException {
        if (config == null) {
            throw new PSSException("null config");
        } else if (config.getParams() == null) {
            throw new PSSException("no config parameters set");
        }
        
        this.config = config;
        String configFileName = (String)config.getParams().get("configFile");
        if (configFileName == null) {
            throw new PSSException("required configFile parameter not set");
        }

        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        try {
            yamlFilename = cc.getFilePath(configFileName);

        } catch (ConfigException e) {
            throw new PSSException(e);
        }


    }


}
