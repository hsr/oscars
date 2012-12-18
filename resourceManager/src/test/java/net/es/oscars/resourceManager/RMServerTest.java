package net.es.oscars.resourceManager;

import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import static java.util.Arrays.asList;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;


import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.resourceManager.soap.gen.StoreReqContent;
import net.es.oscars.resourceManager.soap.gen.StoreRespContent;
import net.es.oscars.api.soap.gen.v06.GlobalReservationId;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.common.soap.gen.AuthConditionType;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.OSCARSFault;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.resourceManager.soap.gen.UpdateStatusReqContent;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.api.soap.gen.v06.Layer3Info;
import net.es.oscars.api.soap.gen.v06.Layer2Info;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.VlanTag;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

public final class RMServerTest {

    private static Logger LOG = null;
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_RM);
    private static String context = ConfigDefaults.CTX_DEVELOPMENT;
    public static final String DEFAULT_CONFIG_FILE = "store.yaml";
    private static String operation = null;
    private static String gri = null;
    private static Integer numReq = null;
    private static Integer offset = 0;
    private static Boolean hopsAllowed = true;
    private static String paramFile = DEFAULT_CONFIG_FILE;
    private static String status = "INVALID";
    /**
     *
     * @param args -c operation, -gri gri for query, -n number requested for list
     *          -o for offset in list, -pf paramFile for store, -s status for updateStatus
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static void main(String args[]) throws Exception {
        OSCARSNetLogger netLog = new OSCARSNetLogger(ModuleName.RM);
 
        cc.setContext(context);
        cc.setServiceName(ServiceNames.SVC_RM);
        try {
            System.out.println("loading manifest from ./config/"+ConfigDefaults.MANIFEST);
            cc.loadManifest(ServiceNames.SVC_RM,  ConfigDefaults.MANIFEST); // manifest.yaml
            cc.setLog4j();
        } catch (ConfigException ex) {
            System.out.println("caught ConfigurationException " + ex.getMessage());
            System.exit(-1);
        }
        LOG = Logger.getLogger(RMServerTest.class);

        String configFile =cc.getFilePath(ConfigDefaults.CONFIG);
        Map config = ConfigHelper.getConfiguration(configFile);
        assert config != null : "No configuration";
        Map authN = (Map) config.get("soap");
        assert authN != null : "No soap stanza in configuration";
        URL host = new URL((String)authN.get("publishTo"));
        URL wsdl = cc.getWSDLPath(null);
        System.out.println("host is " + host.toString() + "wsdl is " + wsdl.toString());

        LOG.info(netLog.start("Connecting to: " + host.toString()));

        parseArgs(args);
        RMClient client = RMClient.getClient (host, wsdl);
        AuthConditions authConditions = new AuthConditions();
     // empty conditions should be ok
        if (hopsAllowed) {
            AuthConditionType ac = new AuthConditionType();
            ac.setName("internalHopsAllowed");
            ac.getConditionValue().add("true");
            authConditions.getAuthCondition().add(ac);
        }
        MessagePropertiesType msgProps = new MessagePropertiesType();
        msgProps.setGlobalTransactionId("1234");
        SubjectAttributes attrs = new SubjectAttributes();
        msgProps.setOriginator(attrs);
        LOG.info(netLog.end("init"));
        
        if (operation.equals("store")){
            LOG.info(netLog.start("store"));
            System.out.println("calling store reservation with params from " + paramFile);
            ResDetails params = configure(paramFile);
            if (gri != null) {
                // command line value overrides configuration file
                params.setGlobalReservationId(gri);
            }
            StoreReqContent storeReqContent = new StoreReqContent();
            storeReqContent.setTransactionId("1234");
            storeReqContent.setReservation(params);
            Object[] req = new Object[]{storeReqContent};
            try {
                Object [] res = client.invoke("store",req);
                // no values are returned, call query to be sure it worked
                QueryResContent query = new QueryResContent();
                query.setMessageProperties(msgProps);
                query.setGlobalReservationId(gri);
                req = new Object[]{authConditions,query};
                res = client.invoke("queryReservation",req);
                QueryResReply reply = (QueryResReply)res[0];
                ResDetails resDetails = reply.getReservationDetails();
                printResDetails(resDetails); 
            } catch (OSCARSServiceException ex) {
                System.out.println("OSCARSServiceException thrown: " + ex.getMessage());
                ErrorReport errReport = ex.getErrorReport();
                if (errReport !=null) {
                    System.out.println(errReport.toString() );
                }
            }
            LOG.info(netLog.end("store"));
        }
        if (operation.equals("query"))
        {
            LOG.debug(netLog.start("queryReservation"));
            QueryResContent query = new QueryResContent();
            query.setMessageProperties(msgProps);
            query.setGlobalReservationId(gri);
            Object[] req = new Object[]{authConditions,query};
            try {
                Object[] res = client.invoke("queryReservation",req);
                QueryResReply reply = (QueryResReply)res[0];
                ResDetails resDetails = reply.getReservationDetails();
                printResDetails(resDetails);
            } catch (OSCARSServiceException ex) {
                System.out.println("OSCARSServiceException thrown: " + ex.getMessage());
                ErrorReport errReport = ex.getErrorReport();
                if (errReport !=null) {
                    System.out.println(errReport.toString() );
                }
            }
            LOG.debug(netLog.end("queryReservation"));
        }
        
        if (operation.equals("updateStatus")){
            LOG.debug(netLog.start("updateStatus"));
            UpdateStatusReqContent updateReq = new UpdateStatusReqContent();
            updateReq.setTransactionId("1234");
            updateReq.setGlobalReservationId(gri);
            updateReq.setStatus(status);
            Object[] req = new Object[]{updateReq};
            try {
                Object[] res = client.invoke("updateStatus",req);
                // an empty arg is returned, call store to see if it worked
                GlobalReservationId GRI = new GlobalReservationId();
                GRI.setGri(gri);
                req = new Object[]{authConditions,GRI};
                res = client.invoke("queryReservation",req);
                UpdateStatusReqContent reply = (UpdateStatusReqContent) res[0];
                System.out.println ("status was set to " + reply.getStatus());
            } catch (OSCARSServiceException ex) {
                System.out.println("OSCARSServiceException thrown: " + ex.getMessage());
                ErrorReport errReport = ex.getErrorReport();
                if (errReport !=null) {
                    System.out.println(errReport.toString() );
                }
            }
            LOG.debug(netLog.end("updateStatus"));
        }
 
        if (operation.equals("list"))
        {
            LOG.debug(netLog.start("listReservation"));
            ListRequest req = new ListRequest();
            req.setMessageProperties(msgProps);
            req.setResOffset(offset);
            try {
                req.setResRequested(numReq);
                Object[] soapReq = new Object[]{authConditions,req};
                Object[] resp = client.invoke("listReservations", soapReq);

                ListReply reply = (ListReply) resp[0];
                System.out.println("total number of reservations is " + reply.getTotalResults());
                List<ResDetails> resDetailsList = reply.getResDetails();
                for (ResDetails resDetails: resDetailsList) {
                    printResDetails(resDetails);
                }
            } catch (OSCARSServiceException ex) {
                System.out.println("OSCARSServiceException thrown: " + ex.getMessage());
                ErrorReport errReport = ex.getErrorReport();
                if (errReport !=null) {
                    System.out.println(errReport.toString() );
                }
            }
            LOG.debug(netLog.end("listReservation"));
        }
        System.exit(0);
    }
    public static void parseArgs(String args[])  throws java.io.IOException {
        // create a parser
        OptionParser parser = new OptionParser();
        parser.acceptsAll( asList( "h", "?" ), "show help then exit" );
        OptionSpec<String> CMD = parser.accepts("c", "command:store,query,list,updateStatus").withRequiredArg().ofType(String.class);
        OptionSpec<String> GRISPEC =  parser.accepts( "gri","globalReservationId" ).withRequiredArg().ofType( String.class );
        OptionSpec<Integer> NUM =parser.accepts( "n", "number of reservations requested").withRequiredArg().ofType( Integer.class );
        OptionSpec<Integer> OFFSET =parser.accepts( "o", "offest of reservations requested").withRequiredArg().ofType( Integer.class );
        ArgumentAcceptingOptionSpec<Boolean> HOPS = parser.accepts( "hops", "are hops allowed").withRequiredArg().ofType( Boolean.class );
        OptionSpec<String> PARAMS = parser.accepts("pf", "parameter file for store" ).withRequiredArg().ofType(String.class);
        OptionSpec<String> STATUS = parser.accepts("s", "status parameter for updateStatus").withRequiredArg().ofType(String.class);
        OptionSpec<String> CONTEXT = parser.accepts("C", "context:UNITTEST,DEVELOPMENT,SDK,PRODUCTION").withRequiredArg().ofType(String.class);
        OptionSet options = parser.parse( args );

        // check for help
        if ( options.has( "?" ) || options.has("h")) {
            parser.printHelpOn( System.out );
            System.exit(0);
        }
        if (options.has (CMD)){
            operation = options.valueOf(CMD);
        } else {
            parser.printHelpOn( System.out);
            System.exit(-1);
        }
        if (options.has(CONTEXT) ){
            context = options.valueOf(CONTEXT);
            if (!context.equals("UNITTEST") &&
                    !context.equals("SDK") &&
                    !context.equals("DEVELOPMENT") &&
                    !context.equals("PRODUCTION") ) 
            {
                System.out.println("unrecognized CONTEXT value: " + context);
                System.exit(-1);
            }
        } 
        if (options.has("gri")) {
            gri=options.valueOf(GRISPEC);
        }
        if (options.has("n")){
            numReq = options.valueOf(NUM);
        }
        if (options.has("o")){
            offset = options.valueOf(OFFSET);
        }
        if (options.has("hops")){
            hopsAllowed = options.valueOf(HOPS);
        }
        if (options.has("pf")) {
            paramFile = options.valueOf(PARAMS);
        }
        if (options.has("s")){
            status = options.valueOf(STATUS);
        }

        if ((operation.equals("query")  || operation.equals("updateStatus")) && gri == null){
            System.out.println("command requires gri argument");
            System.exit(-1);
        }
    }
    /*
     * fills in resDetails from the configuration file
     */
    @SuppressWarnings("unchecked")
    public static ResDetails configure(String configFile) {
        if (configFile == null) {
            configFile = DEFAULT_CONFIG_FILE;
        }

        Map config = ConfigHelper.getConfiguration(configFile);
        Map store = (Map) config.get("store");

        ResDetails resDetails = new ResDetails();
        String gri = (String) store.get("gri");
        String login = (String) store.get("login");
        String layer = (String)store.get("layer");
        Integer bandwidth = (Integer) store.get("bandwidth");
        String src = (String) store.get("src");
        String dst = (String) store.get("dst");
        String description = (String) store.get("description");
        String srcVlan = (String) store.get("srcvlan");
 
        String dstVlan = (String) store.get("dstvlan");
        String start_time = (String) store.get("start-time");
        String end_time = (String) store.get("end-time");
        ArrayList<String> pathArray = (ArrayList<String>) store.get("path");
        String pathSetupMode = (String) store.get("path-setup-mode");

        // 0 means unchanged in update case
        if (!layer.equals("0") && !layer.equals("2") && !layer.equals("3")) {
            die("Layer must be 0 or 2 or 3");
        }
        if (src == null || src.equals("")) {
            die("Source must be specified");
        }
        if (dst == null || dst.equals("")) {
            die("Destination must be specified");
        }
        if (bandwidth == null) {
            die("bandwidth must be specified");
        }
        if (description == null || description.equals("")) {
            die("description must be specified");
        }
        // gri must be set for resourceManager store
        if (gri.equals("") ) {
            gri = "test-1";
        }
        HashMap<String, Long> times = parseTimes(start_time, end_time);

        resDetails.setGlobalReservationId(gri);
        resDetails.setDescription(description);
        resDetails.setCreateTime(times.get("create"));
        if (login != null && !login.equals("") ) {
            resDetails.setLogin(login);
        }
        resDetails.setStatus("INCREATE");
        UserRequestConstraintType uc = new UserRequestConstraintType();
        uc.setBandwidth(bandwidth);
        uc.setStartTime(times.get("start"));
        uc.setEndTime(times.get("end"));
        PathInfo pathInfo =  new PathInfo();
        pathInfo.setPathSetupMode(pathSetupMode);
        if (layer.equals("2")) {
            Layer2Info layer2Info = new Layer2Info();
            layer2Info.setSrcEndpoint(src);
            layer2Info.setDestEndpoint(dst);
            if (srcVlan != null) {
                VlanTag vlan = new VlanTag();
                vlan.setValue(srcVlan);
                vlan.setTagged(true);
                layer2Info.setSrcVtag(vlan);
            }
            if (dstVlan != null) {
                VlanTag vlan = new VlanTag();
                vlan.setValue(dstVlan);
                vlan.setTagged(true);
                layer2Info.setDestVtag(vlan);
            }
            pathInfo.setLayer2Info(layer2Info);
        } else if (layer.equals("3")) {
            // TODO
        }
        uc.setPathInfo(pathInfo);
        resDetails.setUserRequestConstraint(uc);
        
        if ( pathArray !=null && !pathArray.isEmpty() ) {
            // put path elements into a reservedConstraint
            ReservedConstraintType rc = new ReservedConstraintType();
            rc.setBandwidth(bandwidth);
            rc.setStartTime(times.get("start"));
            rc.setEndTime(times.get("end"));
            PathInfo resPathInfo = new PathInfo();
            // set pathInfo for the reservedConstraint
            resPathInfo.setPathSetupMode(pathSetupMode);
            if (layer.equals("2")) {
                Layer2Info layer2Info = new Layer2Info();
                layer2Info.setSrcEndpoint(src);
                layer2Info.setDestEndpoint(dst);
                if (srcVlan != null) {
                    VlanTag vlan = new VlanTag();
                    vlan.setValue(srcVlan);
                    vlan.setTagged(true);
                    layer2Info.setSrcVtag(vlan);
                }
                if (dstVlan != null) {
                    VlanTag vlan = new VlanTag();
                    vlan.setValue(dstVlan);
                    vlan.setTagged(true);
                    layer2Info.setDestVtag(vlan);
                }
                resPathInfo.setLayer2Info(layer2Info);
            } else if (layer.equals("3")) {
                // TODO
            }
            // add to path to resPathInfo  
            resPathInfo.setPathSetupMode(pathSetupMode);
            resPathInfo.setPathType("strict");
            CtrlPlanePathContent path = new CtrlPlanePathContent ();
            List<CtrlPlaneHopContent> hops = path.getHop();
            for (String hop : pathArray) {
               CtrlPlaneHopContent cpHop = new CtrlPlaneHopContent();
               cpHop.setLinkIdRef(hop);
               hops.add(cpHop);
            }
            resPathInfo.setPath(path);
            rc.setPathInfo(resPathInfo);
            resDetails.setReservedConstraint(rc);
        }
 
        return resDetails;
    }
     
    private static void die(String msg) {
        System.err.println("msg: " + msg);
        System.exit(1);
    }
    private static HashMap<String, Long> parseTimes(String start_time, String end_time) {
        HashMap<String, Long> result = new HashMap<String, Long>();
        Long startTime = 0L;
        Long endTime = 0L;
        Long createTime= System.currentTimeMillis()/1000;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (start_time == null || start_time.equals("now") || start_time.equals("")) {
            startTime = System.currentTimeMillis()/1000;
        } else {
            try {
                startTime = df.parse(start_time.trim()).getTime()/1000;
            } catch (java.text.ParseException ex) {
                die("Error parsing start date: "+ex.getMessage());
            }
        }
        if (end_time == null || end_time.equals("")) {
            die("No end time specified.");
        } else if (end_time.startsWith("+")) {
            String[] hm = end_time.substring(1).split("\\:");
            if (hm.length != 2) {
                die("Error parsing end date format");
            } 
            try {
                Integer seconds = Integer.valueOf(hm[0])*3600;
                seconds += Integer.valueOf(hm[1])*60;
                if (seconds < 60) {
                    die("Duration must be > 60 sec");
                }
                endTime = startTime + seconds;
            } catch (NumberFormatException ex) {
                die("Error parsing end date format: "+ex.getMessage());
            }
        } else {
            try {
                endTime = df.parse(end_time.trim()).getTime()/1000;
            } catch (java.text.ParseException ex) {
                die("Error parsing emd date: "+ex.getMessage());
            }
        }
        
        
        result.put("start", startTime);
        result.put("end", endTime);
        result.put("create", createTime);
        return result;
    }

    public static void printResDetails(ResDetails resDetails) {
        System.out.println("\nGRI: " + resDetails.getGlobalReservationId());
        System.out.println("Login: " + resDetails.getLogin());
        System.out.println("Status: "
                + resDetails.getStatus().toString());
        UserRequestConstraintType uConstraint = resDetails.getUserRequestConstraint();
        System.out.println("startTime: " + new Date(uConstraint.getStartTime()*1000).toString());
        System.out.println("endTime: " + new Date(uConstraint.getEndTime()*1000).toString());
        System.out.println("bandwidth: " + Integer.toString(uConstraint.getBandwidth()));
        PathInfo pathInfo = null;
        String pathType = "reserved";
        ReservedConstraintType rConstraint = resDetails.getReservedConstraint();
        if (rConstraint !=  null) {
            pathInfo=rConstraint.getPathInfo();
        } else {
            if (uConstraint ==null) {
                System.out.println("invalid reservation, no reserved or requested path");
                return;
            }
            pathInfo=uConstraint.getPathInfo();
            pathType = "requested";
            System.out.println("no path reserved, using requested path ");
        }
        Layer3Info layer3Info = pathInfo.getLayer3Info();
        if (layer3Info != null) {
            System.out.println("Source host: " +
                    layer3Info.getSrcHost());
            System.out.println("Destination host: " +
                    layer3Info.getDestHost());
        } else {
            Layer2Info layer2Info = pathInfo.getLayer2Info();
            if (layer2Info != null) {
                System.out.println("Source urn: " +
                        layer2Info.getSrcEndpoint());
                System.out.println("Destination urn: " +
                        layer2Info.getDestEndpoint());
            }
        }
        System.out.println(" ");
        CtrlPlanePathContent path = pathInfo.getPath();
        if (path != null) {
            List<CtrlPlaneHopContent> hops = path.getHop();
            if (hops.size() > 0) {
                System.out.println("Hops in " + pathType + " path are:");
                for ( CtrlPlaneHopContent ctrlHop : hops ) {
                    CtrlPlaneLinkContent link = ctrlHop.getLink();
                    if (link != null ) {
                         System.out.println(link.getId());
                    } else {
                        String id = ctrlHop.getLinkIdRef();
                        System.out.println(id);
                    }
                }
            }
        } else {
            System.out.println("no path in " + pathType + " constraint");
        }
    }
}
