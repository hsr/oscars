package net.es.oscars.pss.bridge.test;

import net.es.oscars.api.soap.gen.v06.Layer2Info;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.VlanTag;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;

public class RequestFactory {
    public static ResDetails getTwoHop() {
        String gri = "twoHop-768";
        String srcEdge = "urn:ogf:network:sc11.org:edge-1:et-0/0/0:edge";
        String hop1    = "urn:ogf:network:sc11.org:edge-1:et-2/0/0:edge-2";
        String hop2    = "urn:ogf:network:sc11.org:edge-2:1/1/1:edge-1";
        String hop3    = "urn:ogf:network:sc11.org:edge-2:2/1/1:core-1";
        String hop4    = "urn:ogf:network:sc11.org:core-1:core-2:core-2";
        String dstEdge = "urn:ogf:network:sc11.org:core-1:l2-1214:edge";

        ResDetails resDet = new ResDetails();
        ReservedConstraintType rc = new ReservedConstraintType();
        PathInfo pathInfo                   = new PathInfo();
        Layer2Info l2Info                   = new Layer2Info();
        VlanTag srcVlan                     = new VlanTag();
        VlanTag dstVlan                     = new VlanTag();
        srcVlan.setTagged(true);
        srcVlan.setValue("998");
        dstVlan.setTagged(true);
        dstVlan.setValue("998");
        rc.setBandwidth(1000);

        
        pathInfo.setLayer2Info(l2Info);
        l2Info.setSrcEndpoint(srcEdge);
        l2Info.setDestEndpoint(dstEdge);
        l2Info.setSrcVtag(srcVlan);
        l2Info.setDestVtag(dstVlan);
        CtrlPlanePathContent path           = new CtrlPlanePathContent();

        CtrlPlaneHopContent srcHop          = RequestFactory.makeEdgeHop(srcEdge, "998");
        
        CtrlPlaneHopContent cpHop1          = RequestFactory.makeEdgeHop(hop1, "998");
        CtrlPlaneHopContent cpHop2          = RequestFactory.makeEdgeHop(hop2, "998");
        CtrlPlaneHopContent cpHop3          = RequestFactory.makeEdgeHop(hop3, "998");
        CtrlPlaneHopContent cpHop4          = RequestFactory.makeEdgeHop(hop4, "998");
        
        
        

        CtrlPlaneHopContent dstHop          = RequestFactory.makeEdgeHop(dstEdge, "998");
        

        resDet.setGlobalReservationId(gri);
        resDet.setReservedConstraint(rc);
        
        

        rc.setPathInfo(pathInfo);
        pathInfo.setPath(path);
        path.getHop().add(srcHop);
        path.getHop().add(cpHop1);
        path.getHop().add(cpHop2);
        path.getHop().add(cpHop3);
        path.getHop().add(cpHop4);
        path.getHop().add(dstHop);
        return resDet;
    }
    public static ResDetails getAB() {
        String gri = "oneHop-311";
        String srcEdge = "urn:ogf:network:sc11.org:edge-2:1/1/3:edge";
        String hop1    = "urn:ogf:network:sc11.org:edge-2:2/1/1:core-1";
        String hop2    = "urn:ogf:network:sc11.org:core-1:10/1/1:edge-2";
        String dstEdge = "urn:ogf:network:sc11.org:core-1:20/1/1:edge";

        ResDetails resDet = new ResDetails();
        ReservedConstraintType rc = new ReservedConstraintType();
        PathInfo pathInfo                   = new PathInfo();
        Layer2Info l2Info                   = new Layer2Info();
        VlanTag srcVlan                     = new VlanTag();
        VlanTag dstVlan                     = new VlanTag();
        srcVlan.setTagged(true);
        srcVlan.setValue("998");
        dstVlan.setTagged(true);
        dstVlan.setValue("998");
        rc.setBandwidth(1000);

        
        pathInfo.setLayer2Info(l2Info);
        l2Info.setSrcEndpoint(srcEdge);
        l2Info.setDestEndpoint(dstEdge);
        l2Info.setSrcVtag(srcVlan);
        l2Info.setDestVtag(dstVlan);
        CtrlPlanePathContent path           = new CtrlPlanePathContent();

        CtrlPlaneHopContent srcHop          = RequestFactory.makeEdgeHop(srcEdge, "998");
        
        CtrlPlaneHopContent cpHop1          = RequestFactory.makeEdgeHop(hop1, "998");
        CtrlPlaneHopContent cpHop2          = RequestFactory.makeEdgeHop(hop2, "998");
        
        
        

        CtrlPlaneHopContent dstHop          = RequestFactory.makeEdgeHop(dstEdge, "998");
        

        resDet.setGlobalReservationId(gri);
        resDet.setReservedConstraint(rc);
        
        

        rc.setPathInfo(pathInfo);
        pathInfo.setPath(path);
        path.getHop().add(srcHop);
        path.getHop().add(cpHop1);
        path.getHop().add(cpHop2);
        path.getHop().add(dstHop);
        return resDet;
    }
    
    public static ResDetails getCD() {
        String gri = "oneHopCD-311";
        String srcEdge = "urn:ogf:network:sc11.org:edge-2:1/1/3:edge";
        String hop1    = "urn:ogf:network:sc11.org:edge-2:3/1/1:core-2";
        String hop2    = "urn:ogf:network:sc11.org:core-2:1/1/1:edge-2";
        String dstEdge = "urn:ogf:network:sc11.org:core-2:3/1/1:edge";

        ResDetails resDet = new ResDetails();
        ReservedConstraintType rc = new ReservedConstraintType();
        PathInfo pathInfo                   = new PathInfo();
        Layer2Info l2Info                   = new Layer2Info();
        VlanTag srcVlan                     = new VlanTag();
        VlanTag dstVlan                     = new VlanTag();
        srcVlan.setTagged(true);
        srcVlan.setValue("998");
        dstVlan.setTagged(true);
        dstVlan.setValue("998");
        rc.setBandwidth(1000);

        
        pathInfo.setLayer2Info(l2Info);
        l2Info.setSrcEndpoint(srcEdge);
        l2Info.setDestEndpoint(dstEdge);
        l2Info.setSrcVtag(srcVlan);
        l2Info.setDestVtag(dstVlan);
        CtrlPlanePathContent path           = new CtrlPlanePathContent();

        CtrlPlaneHopContent srcHop          = RequestFactory.makeEdgeHop(srcEdge, "998");
        
        CtrlPlaneHopContent cpHop1          = RequestFactory.makeEdgeHop(hop1, "998");
        CtrlPlaneHopContent cpHop2          = RequestFactory.makeEdgeHop(hop2, "998");
        
        
        

        CtrlPlaneHopContent dstHop          = RequestFactory.makeEdgeHop(dstEdge, "998");
        

        resDet.setGlobalReservationId(gri);
        resDet.setReservedConstraint(rc);
        
        

        rc.setPathInfo(pathInfo);
        pathInfo.setPath(path);
        path.getHop().add(srcHop);
        path.getHop().add(cpHop1);
        path.getHop().add(cpHop2);
        path.getHop().add(dstHop);
        return resDet;
    }
    
    
    public static ResDetails getSameDevice() {
        String gri = "sameDev-333";
        String srcEdge = "urn:ogf:network:sc11.org:edge-1:1/1/0:edge";
        String dstEdge = "urn:ogf:network:sc11.org:edge-1:2/1/0:edge";

        ResDetails resDet = new ResDetails();
        ReservedConstraintType rc = new ReservedConstraintType();
        PathInfo pathInfo                   = new PathInfo();
        Layer2Info l2Info                   = new Layer2Info();
        VlanTag srcVlan                     = new VlanTag();
        VlanTag dstVlan                     = new VlanTag();
        srcVlan.setTagged(true);
        srcVlan.setValue("998");
        dstVlan.setTagged(true);
        dstVlan.setValue("998");
        rc.setBandwidth(1000);
        
        
        pathInfo.setLayer2Info(l2Info);
        l2Info.setSrcEndpoint(srcEdge);
        l2Info.setDestEndpoint(dstEdge);
        l2Info.setSrcVtag(srcVlan);
        l2Info.setDestVtag(dstVlan);
        
        CtrlPlanePathContent path           = new CtrlPlanePathContent();

        CtrlPlaneHopContent srcHop          = RequestFactory.makeEdgeHop(srcEdge, "998");
        CtrlPlaneHopContent dstHop          = RequestFactory.makeEdgeHop(dstEdge, "998");
        
        
        resDet.setGlobalReservationId(gri);
        resDet.setReservedConstraint(rc);
        
        

        rc.setPathInfo(pathInfo);
        pathInfo.setPath(path);
        path.getHop().add(srcHop);
        path.getHop().add(dstHop);
        return resDet;        
    }
    
    public static CtrlPlaneHopContent makeInternalHop(String linkId) {
        CtrlPlaneHopContent hop          = new CtrlPlaneHopContent();
        CtrlPlaneLinkContent link        = new CtrlPlaneLinkContent();
        hop.setLinkIdRef(linkId);
        link.setId(linkId);
        hop.setLink(link);
        return hop;
        
    }
    
    public static CtrlPlaneHopContent makeEdgeHop(String linkId, String vlan) {
        
        CtrlPlaneHopContent hop          = new CtrlPlaneHopContent();
        
        CtrlPlaneLinkContent link        = new CtrlPlaneLinkContent();
        CtrlPlaneSwcapContent scp        = new CtrlPlaneSwcapContent();
        CtrlPlaneSwitchingCapabilitySpecificInfo ssi
                                         = new CtrlPlaneSwitchingCapabilitySpecificInfo();
        
        hop.setLinkIdRef(linkId);
        link.setId(linkId);
        ssi.setSuggestedVLANRange("998");
        ssi.setVlanRangeAvailability("998");
        scp.setSwitchingCapabilitySpecificInfo(ssi);
        link.setSwitchingCapabilityDescriptors(scp);
        hop.setLink(link);
        return hop;
    }
    
}
