package net.es.oscars.client.examples;

import java.util.Date;
import java.util.List;
import net.es.oscars.api.soap.gen.v06.*;
import org.ogf.schema.network.topology.ctrlplane.*;

public class OSCARSClientUtils {

         /**
     * print out all information about this reservation
     * @param resDetails Must contain a userConstraint, may contain a reservedConstaint
     *       if reservedConstraint exists, use info from it, otherwise use userConstraint.
     *       The pathInfo element may contain one of layer2Info or layer3Info. It may also contain
     *       a path structure. If the path structure exists the info from it will be used rather than
     *       data from layer2Info. Layer3 is currently not implemented
     */
    public static void printResDetails(ResDetails resDetails) {
        System.out.println("\nGRI: " + resDetails.getGlobalReservationId());
        System.out.println("Login: " + resDetails.getLogin());
        System.out.println("Description: " + resDetails.getDescription());
        System.out.println("Status: "
                + resDetails.getStatus().toString());
        UserRequestConstraintType uConstraint = resDetails.getUserRequestConstraint();
        System.out.println("startTime: " + new Date(uConstraint.getStartTime()*1000).toString());
        System.out.println("endTime: " + new Date(uConstraint.getEndTime()*1000).toString());
        System.out.println("bandwidth: " + Integer.toString(uConstraint.getBandwidth()));
        PathInfo pathInfo = null;
        String pathType = null;
        ReservedConstraintType rConstraint = resDetails.getReservedConstraint();
        if (rConstraint !=  null) {
            pathInfo=rConstraint.getPathInfo();
            pathType = "reserved";
        } else {
            uConstraint = resDetails.getUserRequestConstraint();
            if (uConstraint ==null) {
                System.out.println("invalid reservation, no reserved or requested path");
                return;
            }
            pathInfo=uConstraint.getPathInfo();
            pathType="requested";
            System.out.println("no path reserved, using requested path ");
        }
        Layer3Info layer3Info = pathInfo.getLayer3Info();
        if (layer3Info != null) {
            System.out.println("Source host: " +
                    layer3Info.getSrcHost());
            System.out.println("Destination host: " +
                    layer3Info.getDestHost());
        }
        CtrlPlanePathContent path = pathInfo.getPath();
        if (path != null) {
            List<CtrlPlaneHopContent> hops = path.getHop();
            if (hops.size() > 0) {
                System.out.println("Hops in " + pathType + " path are:");
                for ( CtrlPlaneHopContent ctrlHop : hops ) {
                    CtrlPlaneLinkContent link = ctrlHop.getLink();
                    String vlanRangeAvail = "any";
                    if (link != null ) {
                        CtrlPlaneSwcapContent swcap= link.getSwitchingCapabilityDescriptors();
                        if (swcap != null) {
                            CtrlPlaneSwitchingCapabilitySpecificInfo specInfo = swcap.getSwitchingCapabilitySpecificInfo();
                            if (specInfo != null) {
                                vlanRangeAvail = specInfo.getVlanRangeAvailability();
                            }
                        }
                        System.out.println(link.getId() + " vlanRange: " + vlanRangeAvail);
                    } else {
                        String id = ctrlHop.getLinkIdRef();
                        System.out.println(id);
                    }
                }
            }
            else {
                Layer2Info layer2Info = pathInfo.getLayer2Info();
                if (layer2Info != null) {
                    String vlanRange = "any";
                    if (layer2Info.getSrcVtag() != null) {
                        vlanRange = layer2Info.getSrcVtag().getValue();
                    }
                    System.out.println("Source urn: " +
                            layer2Info.getSrcEndpoint() + " vlanTag:" + vlanRange);
                    vlanRange = "any";
                    if (layer2Info.getDestVtag() != null) {
                        vlanRange = layer2Info.getDestVtag().getValue();
                    }
                    System.out.println("Destination urn: " +
                            layer2Info.getDestEndpoint() + " vlanTag:" + vlanRange);
                }
            }
        } else {
            System.out.println("no path information in " + pathType + " constraint");
        }
    }
}