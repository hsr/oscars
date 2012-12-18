package net.es.oscars.pss.test.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.pss.beans.PSSRequest;
import net.es.oscars.pss.soap.gen.SetupReqContent;
import net.es.oscars.pss.soap.gen.TeardownReqContent;
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

public class SimRequestGenerator {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ArrayList<SimRequest> makeRequests(Map config) {
        ArrayList<SimRequest> requests = new ArrayList<SimRequest>();

        // List<String> routerNames = (List<String>) config.get("routers");

        Map simParams = (Map) config.get("sim");
        Integer reqNum = (Integer) simParams.get("requests");
        Integer griNum = (Integer) simParams.get("gris");

        Integer setupMinTime = (Integer) simParams.get("setupmintime");
        Integer setupMaxTime = (Integer) simParams.get("setupmaxtime");
        Integer maxTimeBetween = (Integer) simParams.get("maxtimebetween");
        String localDomainId = (String) simParams.get("localdomain");

        assert (reqNum != null && reqNum > 0) : "No requests";
        assert setupMinTime != null : "No setupmintime";
        assert setupMaxTime != null : "No setupmaxtime";
        assert maxTimeBetween != null : "No maxtimebetween";
        assert localDomainId != null : "No localDomainId";

        ArrayList<HashMap<String, String>> resvs = new ArrayList<HashMap<String, String>>();
        List<String> edgePorts = (List<String>) config.get("edge-ports");
        Random rand = new Random();

        for (int i = 0; i < griNum; i++) {
            String gri = "gri-"+localDomainId+"-"+i;
            Integer iEdge = rand.nextInt(edgePorts.size());
            String srcEdge = edgePorts.get(iEdge);
            String dstEdge = srcEdge;
            while (srcEdge.equals(dstEdge)) {
                iEdge = rand.nextInt(edgePorts.size());
                dstEdge = edgePorts.get(iEdge);
            }
            srcEdge = "urn:ogf:network:"+localDomainId+":"+srcEdge+":edge";
            dstEdge = "urn:ogf:network:"+localDomainId+":"+dstEdge+":edge";
            String srcRouter = "";
            String dstRouter = "";
            URNParserResult parseRes = URNParser.parseTopoIdent(srcEdge);
            srcRouter = parseRes.getNodeId();
            parseRes = URNParser.parseTopoIdent(dstEdge);
            dstRouter = parseRes.getNodeId();


            HashMap<String, String> edges = new HashMap<String, String>();
            edges.put("gri", gri);
            edges.put("srcEdge", srcEdge);
            edges.put("dstEdge", dstEdge);
            edges.put("srcRouter", srcRouter);
            edges.put("dstRouter", dstRouter);
            resvs.add(edges);
        }



        Integer lastRequestTime = 0;
        for (int i = 0; i < reqNum; i++) {
            Integer thisRequestTime = lastRequestTime + rand.nextInt(maxTimeBetween+1);
            lastRequestTime = thisRequestTime;
            Integer srcSetupTime = setupMinTime + rand.nextInt(setupMaxTime - setupMinTime);
            Integer dstSetupTime = setupMinTime + rand.nextInt(setupMaxTime - setupMinTime);

            SimRequest req = new SimRequest();
            req.setSrcSetupTime(srcSetupTime);
            req.setDstSetupTime(dstSetupTime);
            req.setStartTime(thisRequestTime);

            PSSRequest.PSSRequestTypes op = PSSRequest.PSSRequestTypes.SETUP;
            if (rand.nextInt() % 2 != 0) {
                op = PSSRequest.PSSRequestTypes.TEARDOWN;
            }
            req.setRequestType(op);


            HashMap<String, String> resv = resvs.get(rand.nextInt(resvs.size()));
            String gri = resv.get("gri");
            String srcEdge = resv.get("srcEdge");
            String dstEdge = resv.get("dstEdge");
            String srcRouter = resv.get("srcRouter");
            String dstRouter = resv.get("dstRouter");

            req.setId("request-"+i);
            System.out.println("req id: "+req.getId());
            System.out.println("    time: "+thisRequestTime);
            System.out.println("    srctime:  "+srcSetupTime);
            System.out.println("    dsttime:  "+dstSetupTime);
            System.out.println("    srcEdge: "+srcEdge);
            System.out.println("    dstEdge: "+dstEdge);
            System.out.println("    srcRouter: "+srcRouter);
            System.out.println("    dstRouter: "+dstRouter);
            System.out.println("    gri: "+gri);
            System.out.println("    op: "+req.getRequestType());



            ResDetails resDet = new ResDetails();
            ReservedConstraintType rc = new ReservedConstraintType();
            PathInfo pathInfo                   = new PathInfo();
            CtrlPlanePathContent path           = new CtrlPlanePathContent();
            SetupReqContent setupReq            = new SetupReqContent();
            TeardownReqContent teardownReq      = new TeardownReqContent();
            CtrlPlaneHopContent srcHop          = new CtrlPlaneHopContent();
            srcHop.setLinkIdRef(srcEdge);
            CtrlPlaneHopContent dstHop          = new CtrlPlaneHopContent();
            CtrlPlaneLinkContent dstLink        = new CtrlPlaneLinkContent();
            dstHop.setLinkIdRef(dstEdge);
            CtrlPlaneLinkContent srcLink        = new CtrlPlaneLinkContent();
            srcLink.setId(srcEdge);
            dstLink.setId(srcEdge);
            srcHop.setLink(srcLink);
            dstHop.setLink(dstLink);

            resDet.setGlobalReservationId(gri);
            resDet.setReservedConstraint(rc);
            rc.setPathInfo(pathInfo);
            pathInfo.setPath(path);
            path.getHop().add(srcHop);
            path.getHop().add(dstHop);


            if (op.equals(PSSRequest.PSSRequestTypes.SETUP)) {
                setupReq.setReservation(resDet);

                req.setSetupReq(setupReq);
            } else {
                teardownReq.setReservation(resDet);
                req.setTeardownReq(teardownReq);
            }

            requests.add(req);
        }
        return requests;
    }

}
