package net.es.oscars.authN.dao;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.authN.beans.Institution;

/**
 * InstitutionDAO is the data access object for the authN.institutions
 * table.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class InstitutionDAO
        extends GenericHibernateDAO<Institution, Integer> {

    public InstitutionDAO(String dbname) {
        this.setDatabase(dbname);
    }
    /**
     * Finds an institution given on its name.
     *
     * @param name the institution name
     * @return institution The corresponding institution instance, if any
     */
    public Institution queryByName(String name) {
        Institution inst = (Institution) this.queryByParam("name", name);
        return inst;
    }

}
