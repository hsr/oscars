package net.es.oscars.pce.dijkstra;

import java.util.ArrayList;

import net.es.oscars.utils.soap.OSCARSServiceException;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

public abstract class LinkEvaluator {
    
    public void initCreate(CtrlPlanePathContent pathConstraints, CtrlPlaneTopologyContent topology) throws OSCARSServiceException{
        return;
    }
    
    public abstract boolean evaluate(CtrlPlaneLinkContent link, ArrayList<CtrlPlaneLinkContent> currentBestPath );
    
    public void finalizeCreate(CtrlPlanePathContent pathConstraints, CtrlPlaneTopologyContent topology) throws OSCARSServiceException{
        return;
    }
    
    public void commit(CtrlPlanePathContent pathConstraints, CtrlPlaneTopologyContent topology) throws OSCARSServiceException{
        return;
    }
}
