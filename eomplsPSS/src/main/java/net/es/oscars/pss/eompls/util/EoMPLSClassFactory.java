package net.es.oscars.pss.eompls.util;


import org.apache.log4j.Logger;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
import net.es.oscars.pss.eompls.beans.config.EoMPLSBaseConfig;
import net.es.oscars.pss.eompls.config.EoMPLSConfigHolder;

/**
 * responsible for configuring & loading the various PSS agent classes
 * @author haniotak
 *
 */
public class EoMPLSClassFactory {
   
    private EoMPLSDeviceAddressResolver eomplsDeviceAddressResolver;
    private EoMPLSIfceAddressResolver eomplsIfceAddressResolver;
    
    private static EoMPLSClassFactory instance;
    private Logger log = Logger.getLogger(EoMPLSClassFactory.class);

    /**
     * singleton constructor
     * @return
     */
    private EoMPLSClassFactory() {
    }

    public static EoMPLSClassFactory getInstance() {
        if (instance == null) {
            instance = new EoMPLSClassFactory();
        }
        return instance;
    }

    public void health() throws PSSException {
        if (eomplsDeviceAddressResolver == null) {
            throw new PSSException("eomplsDeviceAddressResolver not set");
        } else if (eomplsIfceAddressResolver == null) {
            throw new PSSException("eomplsIfceAddressResolver not set");
        }
    }


    /**
     * configures the agent factory through YAML from the argument filename
     * loads and configures agent classes
     *
     * @param filename
     */
    public void configure() throws PSSException {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        EoMPLSBaseConfig eomplsBaseConfig = EoMPLSConfigHolder.getInstance().getEomplsBaseConfig();
        if (eomplsBaseConfig == null) {
            log.error("No base configuration stanza!");
            System.err.println("No base configuration stanza!");
            System.exit(1);
        }
        
        String eomplsDevAddrResCN = eomplsBaseConfig.getEomplsDeviceAddressResolve().getImpl();
        String eomplsIfceAddrResCN = eomplsBaseConfig.getEomplsIfceAddressResolve().getImpl();
        Class<?> aClass = null;        
        try {
            if (this.eomplsDeviceAddressResolver == null) { 
                aClass = cl.loadClass(eomplsDevAddrResCN);
                eomplsDeviceAddressResolver = (EoMPLSDeviceAddressResolver) aClass.newInstance();
                eomplsDeviceAddressResolver.setConfig(eomplsBaseConfig.getEomplsDeviceAddressResolve());
                log.debug("eomplsDeviceAddressResolver loaded OK: "+eomplsDevAddrResCN);
            }
            
            if (this.eomplsIfceAddressResolver == null) { 
                aClass = cl.loadClass(eomplsIfceAddrResCN);
                eomplsIfceAddressResolver = (EoMPLSIfceAddressResolver) aClass.newInstance();
                eomplsIfceAddressResolver.setConfig(eomplsBaseConfig.getEomplsIfceAddressResolve());
                log.debug("eomplsIfceAddressResolver loaded OK: "+eomplsIfceAddrResCN);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        
    }

    public EoMPLSDeviceAddressResolver getEomplsDeviceAddressResolver() {
        return eomplsDeviceAddressResolver;
    }

    public void setEomplsDeviceAddressResolver(
            EoMPLSDeviceAddressResolver eomplsDeviceAddressResolver) {
        this.eomplsDeviceAddressResolver = eomplsDeviceAddressResolver;
    }

    public EoMPLSIfceAddressResolver getEomplsIfceAddressResolver() {
        return eomplsIfceAddressResolver;
    }

    public void setEomplsIfceAddressResolver(
            EoMPLSIfceAddressResolver eomplsIfceAddressResolver) {
        this.eomplsIfceAddressResolver = eomplsIfceAddressResolver;
    }




}
