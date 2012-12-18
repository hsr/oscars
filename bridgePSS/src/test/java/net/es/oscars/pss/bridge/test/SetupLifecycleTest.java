package net.es.oscars.pss.bridge.test;



import net.es.oscars.api.soap.gen.v06.ResDetails;

import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.pss.sched.quartz.PSSScheduler;
import net.es.oscars.pss.sched.quartz.WorkflowInspectorJob;
import net.es.oscars.pss.soap.PSSSoapHandler;
import net.es.oscars.pss.soap.gen.SetupReqContent;
import net.es.oscars.pss.soap.gen.TeardownReqContent;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;

import org.apache.log4j.Logger;

import org.testng.SkipException;
import org.testng.annotations.Test;

import java.io.File;

@Test
public class SetupLifecycleTest {
    private Logger log = Logger.getLogger(SetupLifecycleTest.class);
    @Test(groups = { "lifecycle" })
    public void testSetup() throws ConfigException, PSSException {
        PathTools.setLocalDomainId("sc11.org");
        
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        cc.loadManifest(new File("src/test/resources/"+ConfigDefaults.MANIFEST));
        cc.setContext(ConfigDefaults.CTX_TESTING);
        cc.setServiceName(ServiceNames.SVC_PSS);
        

        try {
            String configFilePath = cc.getFilePath(ConfigDefaults.CONFIG);
            System.out.println("loading config from "+configFilePath);
            cc.setLog4j();
            ConfigHolder.loadConfig(configFilePath);
            ClassFactory.getInstance().configure();
        
        } catch (ConfigException ex ) {
            ex.printStackTrace();
            log.debug ("skipping Tests, PSS is  not configured");
            throw new SkipException("skipping Tests, PSS is  not configured");
        }
        
        log.debug("starting PSS main scheduler");
        PSSScheduler sched = PSSScheduler.getInstance();
        try {
            sched.setWorkflowInspector(WorkflowInspectorJob.class);
            sched.start();
        } catch (PSSException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        
        
        

        
        ResDetails resDet;
        
        // same device
        // = RequestFactory.getSameDevice();
        
        // this.testBoth(resDet);
        
        // two hops 
        resDet = RequestFactory.getTwoHop();
        this.testBoth(resDet);
        
        
        /*
        // A->B single hop
        resDet = RequestFactory.getAB();
        this.testBoth(resDet);
        
        // C->D single hop
        resDet = RequestFactory.getCD();
        this.testBoth(resDet);
        */
        log.debug("simulation.run.end");
        PSSScheduler.getInstance().stop();
    }
    private void testBoth(ResDetails resDet) {
        PSSSoapHandler soap = new PSSSoapHandler();

        SetupReqContent     setupReq    = new SetupReqContent();
        TeardownReqContent  tdReq       = new TeardownReqContent();
        setupReq.setReservation(resDet);
        tdReq.setReservation(resDet);
        soap.setup(setupReq);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        soap.teardown(tdReq);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean done = false;
        while (!done) {
            try {
                Thread.sleep(1000);
                if (!ClassFactory.getInstance().getWorkflow().hasOutstanding()) {
                    done = true;
                } else {
                    for (PSSAction act : ClassFactory.getInstance().getWorkflow().getOutstanding()) {
                        System.out.println(act.getRequest().getId()+" "+act.getStatus()+" "+act.getActionType());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
