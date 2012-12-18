package net.es.oscars.pss.eompls.test;


import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.database.hibernate.Initializer;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.pss.eompls.alu.ALUNameGenerator;
import net.es.oscars.pss.eompls.alu.SR_VPLS_ConfigGen;
import net.es.oscars.pss.eompls.beans.ScopedResourceLock;
import net.es.oscars.pss.eompls.common.EoMPLSPSSCore;
import net.es.oscars.pss.eompls.config.EoMPLSConfigHolder;
import net.es.oscars.pss.eompls.dao.ScopedResourceLockDAO;
import net.es.oscars.pss.eompls.junos.MX_VPLS_ConfigGen;
import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Test
public class DBTest {
    private static Logger LOG = Logger.getLogger(DBTest.class.getName());
    private static String dbname = "testeomplspss";

    @Test (groups = {"db"})
    public void testSRL() throws Exception {
        EoMPLSPSSCore core = EoMPLSPSSCore.getInstance();


        core.getSession().beginTransaction();
        ScopedResourceLockDAO srlDAO = new ScopedResourceLockDAO(dbname);
        for (Integer i = 1; i < 20; i++) {
            ScopedResourceLock srl = new ScopedResourceLock();
            srl.setRangeExpr("1-100");
            srl.setResource(i);
            srl.setScope("foo");
            srl.setGri("es.net-2"+i);
            srlDAO.update(srl);
        }

        ScopedResourceLock srla = srlDAO.createSRL("foo:a", "30-50", null, "es.net-233");
        System.out.println(srla.toString());
        srlDAO.update(srla);

        ScopedResourceLock srlb = srlDAO.createSRL("foo:b", "2-99", srla.getResource(), "es.net-240");
        srlDAO.update(srlb);
        System.out.println(srlb.toString());

        List<ScopedResourceLock> srls;
        srls = srlDAO.getByScope("foo");
        for (ScopedResourceLock srl : srls) {
            System.out.println(srl.toString());
            if (srl.getResource().equals(30)) {
                srlDAO.remove(srl);
            }
        }


        srls = srlDAO.getByScopeAndResource("foo", 31);
        for (ScopedResourceLock srl : srls) {
            System.out.println(srl.toString());
        }

        srls = srlDAO.getByScopeAndResource("foo", 30);
        for (ScopedResourceLock srl : srls) {
            System.out.println(srl.toString());
        }
        srlDAO.flush();


    }

}
