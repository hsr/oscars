package net.es.oscars.pss.eompls.dao;

import net.es.oscars.database.hibernate.GenericHibernateDAO;
import net.es.oscars.pss.eompls.beans.ScopedResourceLock;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;



public class ScopedResourceLockDAO
    extends GenericHibernateDAO<ScopedResourceLock, Integer> {

    private Logger log;
    private String dbname;

    public ScopedResourceLockDAO(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.setDatabase(dbname);
        this.dbname = dbname;
    }

    public List<ScopedResourceLock> getByScope(String scope) {
        Criterion c = Restrictions.eq("scope", scope);
        List<ScopedResourceLock> srl = this.findByCriteria(c);
        return srl;
    }

    public List<ScopedResourceLock> getByScopeAndResource(String scope, Integer resource) {
        Criterion c1 = Restrictions.eq("scope", scope);
        Criterion c2 = Restrictions.eq("resource", resource);
        List<ScopedResourceLock> srl = this.findByCriteria(c1, c2);
        return srl;
    }
    public List<ScopedResourceLock> getByScopeAndGri(String scope, String gri) {
        Criterion c1 = Restrictions.eq("scope", scope);
        Criterion c2 = Restrictions.eq("gri", gri);
        List<ScopedResourceLock> srl = this.findByCriteria(c1, c2);
        return srl;
    }


    public void update(ScopedResourceLock srl) {
        super.update(srl);
    }

    public void remove (ScopedResourceLock srl ) {
        super.remove(srl);
    }

    public synchronized ScopedResourceLock createSRL(String scope, String rangeExpr, Integer preferred, String gri) {
        ScopedResourceLock result = new ScopedResourceLock();
        result.setGri(gri);
        result.setRangeExpr(rangeExpr);
        result.setScope(scope);

        List<Integer> lockedResources= new ArrayList<Integer>();

        List<ScopedResourceLock> srls = this.getByScope(scope);
        for (ScopedResourceLock srl : srls) {
            log.debug("SRL found for scope: ["+scope+"]:"+srl.toString());
            if (!lockedResources.contains(srl.getResource())) {
                lockedResources.add(srl.getResource());
            }

        }

        if (preferred != null && lockedResources.contains(preferred)) {
            log.debug("preferred: ["+preferred+"] resource already in use");
        } else if (preferred != null) {
            result.setResource(preferred);
            return result;
        }




        String[] nums = rangeExpr.split("-");
        Integer min = Integer.parseInt(nums[0]);
        Integer max = Integer.parseInt(nums[1]);
        log.debug("min: "+min+" max: "+max);


        for (Integer i = min; i <= max; i++) {
            if (lockedResources.contains(i)) {
                log.debug(i+" resource already in use");
            } else {
                log.debug("found resource "+i+" for scope ["+scope+"]");
                result.setResource(i);
                break;

            }
        }



        return result;

    }

}
