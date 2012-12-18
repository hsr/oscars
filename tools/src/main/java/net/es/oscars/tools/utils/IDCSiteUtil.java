package net.es.oscars.tools.utils;

import java.util.List;
import java.util.Scanner;

import org.hibernate.Session;

import org.apache.log4j.*;

import net.es.oscars.authN.beans.Institution;
import net.es.oscars.authN.common.AuthNCore;
import net.es.oscars.authZ.beans.Site;
import net.es.oscars.authZ.common.AuthZCore;
import net.es.oscars.authZ.dao.SiteDAO;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

public class IDCSiteUtil extends IDCCmdUtil{
    private AuthZCore authzCore = null;
    private AuthNCore authnCore = null;
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_AUTHZ);
    private static ContextConfig authNCC = ContextConfig.getInstance(ServiceNames.SVC_AUTHN);
    private static String context = ConfigDefaults.CTX_PRODUCTION;
    
    public IDCSiteUtil(){
        authzCore = AuthZCore.getInstance();
        authnCore = AuthNCore.getInstance();
        this.dbname = authnCore.getDbname();
    }
    
    private void addSite() {
        Scanner in = new Scanner(System.in);
        
        //Get institution;
        Session authnSession = this.authnCore.getSession();
        authnSession.beginTransaction();
        Institution institution = this.selectInstitution(in, "organization", authnSession);
        authnSession.getTransaction().commit();
        
        //get domain
        String domain = this.readInput(in, "Domain Topology ID", "", false);
        
        //save site
        Session authZsession = authzCore.getSession();
        authZsession.beginTransaction();
        Site site = new Site();
        site.setDomainTopologyId(domain);
        site.setInstitutionName(institution.getName());
        authZsession.save(site);
        authZsession.getTransaction().commit();
        
        System.out.println("New site added for organization '" + institution.getName() + "' and domain '" + domain + "'");
    }

    private void removeSite() {
        Scanner in = new Scanner(System.in);
        Session authZsession = authzCore.getSession();
        authZsession.beginTransaction();
        Site site = this.selectSite(in);
        System.out.print("Are you sure you want to delete '" + 
                site.getInstitutionName() + " - " + 
                site.getDomainTopologyId() +  "'? [y/n] ");
        
        String ans = in.next();
        if(ans.toLowerCase().startsWith("y")){
            authZsession.delete(site);
            System.out.println("Site deleted.");
        }else{
            System.out.println("Operation cancelled. No site deleted.");
        }
        authZsession.getTransaction().commit();
    }
    
    private Site selectSite(Scanner in){
        SiteDAO siteDAO = new SiteDAO(authzCore.getDbname());
        List<Site> sites = siteDAO.list();
        int i = 1;
        
        System.out.println();
        for(Site site : sites){
            System.out.println(i + ". " + site.getInstitutionName() + " - " + site.getDomainTopologyId());
            i++;
        }
        
        System.out.print("Select the site to delete (by number): ");
        int n = in.nextInt();
        in.nextLine();
        
        if(n <= 0 || n > sites.size()){
            System.err.println("Invalid site number '" +n + "' entered");
            System.exit(0);
        }
        
        return sites.get(n-1);
    }
    
    private void listSites(){
        Session authZsession = authzCore.getSession();
        authZsession.beginTransaction();
        SiteDAO siteDAO = new SiteDAO(authzCore.getDbname());
        List<Site> sites = siteDAO.list();
        int i = 1;
        
        System.out.println();
        for(Site site : sites){
            System.out.println(i + ". " + site.getInstitutionName() + " - " + site.getDomainTopologyId());
            i++;
        }
        authZsession.getTransaction().commit();
    }
    
    public static void main(String[] args){
        cc.setContext(context);
        cc.setServiceName(ServiceNames.SVC_AUTHZ);
        authNCC.setContext(context);
        authNCC.setServiceName(ServiceNames.SVC_AUTHN);
        try {
            cc.loadManifest(ServiceNames.SVC_AUTHZ, ConfigDefaults.MANIFEST); // manifest.yaml
            authNCC.loadManifest(ServiceNames.SVC_AUTHN, ConfigDefaults.MANIFEST); // manifest.yaml
            Logger.getRootLogger().setLevel(Level.OFF);
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }
        
        IDCSiteUtil siteUtil = new IDCSiteUtil();
        if(args[0] != null && args[0].equals("remove")){
            siteUtil.removeSite();
        }else if(args[0] != null && args[0].equals("add")){
            siteUtil.addSite();
        }else{
            siteUtil.listSites();
        }
    }


}
