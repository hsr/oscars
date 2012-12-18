package net.es.oscars.pss.eompls.dao;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.pss.eompls.beans.GeneratedConfig;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.List;


public class GeneratedConfigDAO
    extends GenericHibernateDAO<GeneratedConfig, Integer> {

    private Logger log;
    private String dbname;

    public GeneratedConfigDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
    }


    public List<GeneratedConfig> getGC(String phase, String gri, String deviceId) {
        Criterion c1 = Restrictions.eq("phase", phase);
        Criterion c2 = Restrictions.eq("gri", gri);
        Criterion c3 = Restrictions.eq("deviceId", deviceId);
        List<GeneratedConfig> gc = this.findByCriteria(c1, c2, c3);
        return gc;
    }


    public void update(GeneratedConfig gc) {
        super.update(gc);
    }

    public void remove (GeneratedConfig gc ) {
        super.remove(gc);
    }


}
