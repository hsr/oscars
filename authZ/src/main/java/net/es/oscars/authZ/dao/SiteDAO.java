package net.es.oscars.authZ.dao;

import java.util.ArrayList;
import java.util.List;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.authZ.beans.Site;

/**
 * SiteDAO is the data access object for the authZ.sites table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class SiteDAO extends GenericHibernateDAO<Site, Integer> {

    public SiteDAO(String dbname) {
        this.setDatabase(dbname);
    }

/**
 * Get the names of all institutions that contain the domain given by topoId
 * 
 * @param String topoId  topology Identifier of the domain
 * @return List of the institution names that contain this domain
 */
    public List<String> getInstitutions(String topoId) {

        List<String> institutions= new ArrayList<String>();

        // query for all sites were topologyId=topoId
        String hsql = "from Site " + "where domainTopologyId = ? ";
        List<Site> sites = (List<Site>) this.getSession().createQuery(hsql)
            .setString(0, topoId)
            .list();

        for (Site site: sites) {
            institutions.add(site.getInstitutionName());
        }
        return institutions;
    }
    /**
     * Get the topology Identifiers of all sites that are associated with the input institution
     * 
     * @param String institution name topoId   of the domain
     * @return List of the topology Identifiers that are associated with the input institution
     */
        public List<String> getDomains(String institution) {

            List<String> topoIds= new ArrayList<String>();

            // query for all sites were institution= institution
            String hsql = "from Site " + "where institutionName = ? ";
            List<Site> sites = (List<Site>) this.getSession().createQuery(hsql)
                .setString(0, institution)
                .list();

            for (Site site: sites) {
                topoIds.add(site.getDomainTopologyId());
            }
            return topoIds;
        }
}
