package net.es.oscars.nsibridge.test.req;

import net.es.oscars.nsibridge.beans.ProvRequest;
import net.es.oscars.nsibridge.beans.QueryRequest;
import net.es.oscars.nsibridge.beans.TermRequest;
import net.es.oscars.nsibridge.common.Invoker;
import net.es.oscars.nsibridge.common.JettyContainer;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_04.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.impl.ConnectionProvider;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.task.sched.Workflow;
import org.testng.annotations.BeforeSuite;
import net.es.oscars.nsibridge.beans.ResvRequest;
import org.testng.annotations.Test;

import javax.xml.ws.Holder;

public class TaskTest {
    private static ConnectionProvider cp;

    @BeforeSuite (groups = {"task"})
    public void init() throws Exception {
        Invoker i = Invoker.getInstance();
        i.setContext(ConfigDefaults.CTX_TESTING);
        Thread thr = new Thread(i);
        thr.start();

        cp = (ConnectionProvider) JettyContainer.getInstance().getSoapHandlers().get("ConnectionService");
        System.out.print("waiting for jetty.");
        while (cp == null) {
            Thread.sleep(500);
            System.out.print(".");
            cp = (ConnectionProvider) JettyContainer.getInstance().getSoapHandlers().get("ConnectionService");
        }
        System.out.println("\n got jetty!");

    }

    @Test (groups = {"task"})
    public void testTasks() throws Exception {

        ResvRequest rreq = NSIRequestFactory.getRequest();
        ProvRequest preq = NSIRequestFactory.getProvRequest(rreq);
        TermRequest treq = NSIRequestFactory.getTermRequest(preq);
        QueryRequest qreq = NSIRequestFactory.getQueryRequest();

        String connId = rreq.getConnectionId();
        String gri = rreq.getGlobalReservationId();
        String desc = rreq.getDescription();

        Holder<CommonHeaderType> holder = new Holder<CommonHeaderType>();
        holder.value = rreq.getOutHeader();

//        cp.reserve(gri, desc, connId, rreq.getCriteria(), rreq.getInHeader(), holder);

        Thread.sleep(5000);

        cp.provision(connId, preq.getInHeader(), holder);

        Thread.sleep(5000);

        cp.terminate(connId, treq.getInHeader(), holder);

        Thread.sleep(5000);

//        cp.queryNotificationSync(qreq.getOperation(), qreq.getQueryFilter(), qreq.getInHeader(), holder);



        Workflow wf = Workflow.getInstance();
        System.out.println(wf.printTasks());
        while (wf.hasItems()) {
            Thread.sleep(500);
            System.out.println(wf.printTasks());
        }




    }


}
