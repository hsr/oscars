package net.es.oscars.pss.openflowj.util;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.utils.topology.NMWGParserUtil;

public class OFPathTools {
    
    public static String extractNodeId(String fqNodeId) throws PSSException{
        String localNodeId = NMWGParserUtil.normalizeURN(fqNodeId);
        String[] idParts = localNodeId.split(NMWGParserUtil.TOPO_ID_SEPARATOR);
        if(idParts.length == 1){
            return localNodeId;
        }else if(idParts.length == 2){
            return idParts[1];
        }
        throw new PSSException("Invalid node ID extracted from path: " + fqNodeId);
    }
}
