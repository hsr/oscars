package net.es.oscars.pss.resolve;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.es.oscars.pss.api.DeviceAddressResolver;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;

public class DNSDeviceResolver implements DeviceAddressResolver {
    
    private GenericConfig config = null;
    
    public DNSDeviceResolver() {
        
    }
    
    public String getDeviceAddress(String deviceId) throws PSSException {
        String hostname;
        if (this.config == null) {
            hostname = deviceId;
        } else {
            String suffix = (String)this.config.getParams().get("domainSuffix");
            if (suffix == null) {
                suffix = "";
            }
            suffix = suffix.trim();
            if (suffix.length() > 0) {
                if (!suffix.startsWith(".")) {
                    suffix = "."+suffix;
                }
                
                if (!deviceId.endsWith(suffix)) {
                    hostname = deviceId + suffix;
                } else {
                    hostname = deviceId;
                }
            } else {
                hostname = deviceId;
            }
        }
        
        try {
            
            InetAddress addr = Inet4Address.getByName(hostname);
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            throw new PSSException("Unknown host: " + deviceId);
        }
    }

    public void setConfig(GenericConfig config) throws PSSException {
        if (config == null) {
            throw new PSSException("null config");
        } else if (config.getParams() == null) {
            throw new PSSException("no config parameters set");
        }
        
        this.config = config;
        String domainSuffix = (String)config.getParams().get("domainSuffix");
        if (domainSuffix == null) {
            throw new PSSException("required domainSuffix parameter not set");
        }

    }

}
