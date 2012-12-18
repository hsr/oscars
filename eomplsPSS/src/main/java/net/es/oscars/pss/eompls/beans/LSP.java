package net.es.oscars.pss.eompls.beans;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;

import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.NMWGParserUtil;
import net.es.oscars.utils.topology.PathTools;

public class LSP {
    private Logger log = Logger.getLogger(LSP.class);
    private ArrayList<String> pathAddresses;
    private String to;
    private String from;

    public LSP(String deviceId, PathInfo pi, EoMPLSDeviceAddressResolver dar, EoMPLSIfceAddressResolver iar, boolean reverse) throws PSSException {
        log.info("HELLO");
        pathAddresses = new ArrayList<String>();
        
        ArrayList<String> lspLinkIds = new  ArrayList<String>();
        
        String localDomainId = PathTools.getLocalDomainId();
        log.info("local domain: "+localDomainId);
        CtrlPlaneLinkContent ingressLink;
        CtrlPlaneLinkContent egressLink;
        log.info("before links");
        try {
            ingressLink = PathTools.getIngressLink(PathTools.getLocalDomainId(), pi.getPath());
        } catch (OSCARSServiceException e) {
            log.error(e);
            throw new PSSException("PathTools.getIngressLink caught: " + e.getMessage());
        }
        try {
            egressLink = PathTools.getEgressLink(PathTools.getLocalDomainId(), pi.getPath());
        } catch (OSCARSServiceException e) {
            log.error(e);
            throw new PSSException("PathTools.getEgressLink caught: " + e.getMessage());
        }
        if (ingressLink == null) {
            throw new PSSException("Could not determine ingress!");
        }
            
            
        String aLinkId = ingressLink.getId();
        String yLinkId;
        if (reverse) {
            aLinkId = egressLink.getId();
        }
        
        
        
        List<CtrlPlaneHopContent> localHops;
        try {
            localHops = PathTools.getLocalHops(pi.getPath(), localDomainId);
        } catch (OSCARSServiceException e) {
            log.error(e);
            throw (new PSSException("PathTools.getLocalHops caught: " + e.getMessage()));
        } 
        for (int i = 0; i < localHops.size() ; i++) {
            String linkId = localHops.get(i).getLink().getId();
            log.debug("LSP hop: "+linkId);
        }
        
        
        if (reverse) {
            log.debug("reverse lsp hops:");
            for (int i = localHops.size() - 3; i > 0 ; i -= 2) {
                String linkId = localHops.get(i).getLink().getId();
                lspLinkIds.add(linkId);
                log.debug("local lsp hop: "+linkId);
            }
            yLinkId = localHops.get(1).getLink().getId();
        } else {
            log.debug("forward lsp hops:");
            for (int i = 2; i < localHops.size(); i += 2) {
                String linkId = localHops.get(i).getLink().getId();
                lspLinkIds.add(linkId);
                log.debug("local lsp hop: "+linkId);
            }
            yLinkId = localHops.get(localHops.size() - 2).getLink().getId();
        }
        log.debug("alinkId: "+aLinkId);
        log.debug("ylinkId: "+yLinkId);

        for (int i = 0 ; i < lspLinkIds.size(); i++) {
            pathAddresses.add(i, iar.getIfceAddress(lspLinkIds.get(i)));
        }
        
        setFrom(dar.getDeviceAddress(deviceId));
        setTo(iar.getIfceAddress(yLinkId));

    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTo() {
        return to;
    }

    public ArrayList<String> getPathAddresses() {
        return pathAddresses;
    }

    public void setPathAddresses(ArrayList<String> pathAddresses) {
        this.pathAddresses = pathAddresses;
    }
}
