package net.es.oscars.bootstrap;


public class BootStrapJob implements Runnable {

    public final static int COORDINATOR = 1;
    public final static int BWPCE = 2;
    public final static int CONNPCE = 3;
    public final static int API = 4;
    public final static int LOOKUP = 5;
    public final static int RM = 6;
    public final static int AUTHN = 7;
    public final static int AUTHZ = 8;
    public final static int VLANPCE = 9;
    public final static int DIJPCE = 10;
    public final static int NULLAGG = 11;
    public final static int PSS = 12;
    public final static int NOTIFY = 13;
    public final static int TOPO = 14;
    public final static int WBUI = 15;
    public final static int WSNBROKER = 16;

    private int module = 0;
    private String[] args = null;

    public BootStrapJob (int module, String[] args) {
        this.module = module;
        this.args   = args;
    }

    public void run () {

        try {
            switch (this.module) {
                case COORDINATOR:
                    net.es.oscars.coord.common.Invoker.main (this.args);
                    break;
                case BWPCE:
                    net.es.oscars.pce.bandwidth.common.Invoker.main (this.args);
                    break;
                case CONNPCE:
                    net.es.oscars.pce.connectivity.common.Invoker.main (this.args);
                    break;
                case VLANPCE:
                    net.es.oscars.pce.vlan.common.Invoker.main (this.args);
                    break;
                case DIJPCE:
                    net.es.oscars.pce.dijkstra.common.Invoker.main (this.args);
                    break;
                case API:
                    net.es.oscars.api.common.Invoker.main (this.args);
                    break;
                case NULLAGG:
                    net.es.oscars.pce.defaultagg.NullAgg.main (this.args);
                    break;
                case LOOKUP:
                    net.es.oscars.lookup.common.Invoker.main (this.args);
                    break;
                case RM:
                    net.es.oscars.resourceManager.common.Invoker.main (this.args);
                    break;
                case AUTHN:
                    net.es.oscars.authN.common.Invoker.main (this.args);
                    break;
                case AUTHZ:
                    net.es.oscars.authZ.common.Invoker.main (this.args);
                    break;
                case PSS:
                    // net.es.oscars.pss.eompls.common.Invoker.main (this.args);
                    net.es.oscars.pss.stub.common.Invoker.main (this.args);
                    break;
                case NOTIFY:
                    net.es.oscars.notificationBridge.common.Invoker.main (this.args);
                    break;
                case TOPO:
                    net.es.oscars.topoBridge.common.Invoker.main (this.args);
                    break;
                case WBUI:
                    net.es.oscars.wbui.http.WebApp.main (this.args);
                    break;
                case WSNBROKER:
                    net.es.oscars.wsnbroker.common.Invoker.main (this.args);
                    break;


            }
        } catch (Exception e) {
            System.out.println (e);
        }
    }
}

