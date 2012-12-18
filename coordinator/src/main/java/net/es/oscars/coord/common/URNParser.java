package net.es.oscars.coord.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.es.oscars.coord.common.URNParserResult.URNType;

public class URNParser {
    public static URNParserResult parseTopoIdent(String topoIdent, String localDomainId) {
        URNParserResult result = new URNParserResult();

        topoIdent = topoIdent.trim();
        Hashtable<String, String> regexps = new Hashtable<String, String>();
        regexps.put("domainFull", "^urn:ogf:network:domain=([^:]+)$");
        regexps.put("domain", "^urn:ogf:network:([^:=]+)$");
        regexps.put("nodeFull", "^urn:ogf:network:domain=([^:]+):node=([^:]+)$");
        regexps.put("node", "^urn:ogf:network:([^:=]+):([^:=]+)$");
        regexps.put("portFull", "^urn:ogf:network:domain=([^:]+):node=([^:]+):port=([^:]+)$");
        regexps.put("port", "^urn:ogf:network:([^:=]+):([^:=]+):([^:=]+)$");
        regexps.put("linkFull", "^urn:ogf:network:domain=([^:]+):node=([^:]+):port=([^:]+):link=([^:]+)$");
        regexps.put("link", "^urn:ogf:network:([^:=]+):([^:=]+):([^:=]+):([^:=]+)$");

        String domainId = "";
        String nodeId = "";
        String portId = "";
        String linkId = "";
        String matched = "";
        Matcher matcher = null;

        for (String key: regexps.keySet()) {
            Pattern p = Pattern.compile(regexps.get(key));
            matcher = p.matcher(topoIdent);
            if (matcher.matches()) {
                if (key.equals("domain") || key.equals("domainFull")) {
                    matched = "domain";
                    domainId = matcher.group(1);
                } else if (key.equals("node") || key.equals("nodeFull") ) {
                    matched = "node";
                    domainId = matcher.group(1);
                    nodeId = matcher.group(2);
                } else if (key.equals("port") || key.equals("portFull") ) {
                    matched = "port";
                    domainId = matcher.group(1);
                    nodeId = matcher.group(2);
                    portId = matcher.group(3);
                } else if (key.equals("link") || key.equals("linkFull") ) {
                    matched = "link";
                    domainId = matcher.group(1);
                    nodeId = matcher.group(2);
                    portId = matcher.group(3);
                    linkId = matcher.group(4);
                }
            }
        }


        if (topoIdent == null || topoIdent.equals("")) {
            result.setType(URNType.UNKNOWN);
            return result;
        }

        String fqti = "urn:ogf:network";
        String concise = null;
        URNType urnType;

        if (matched == null) {
            try {
                String addressType = "";
                InetAddress[] addrs = InetAddress.getAllByName(topoIdent);
                System.out.print("[Success]:");
                for (int i =0; i < addrs.length;i++){
                    addressType = addrs[i].getClass().getName();
                }

                if (addressType.equals("java.net.Inet6Address")) {
                    urnType = URNType.IPV6ADDR;

                } else if (addressType.equals("java.net.Inet4Address")) {
                    urnType = URNType.IPV4ADDR;
                } else {
                    urnType = URNType.UNKNOWN;
                }
                result.setType(urnType);
                matched = "address";
             } catch (UnknownHostException e) {
                 if (matched == null) {
                     result.setType(URNType.UNKNOWN);
                     return result;
                 }
             }
        } else if (matched.equals("domain")) {
            fqti += ":domain="+domainId;
            result.setDomainId(domainId);
            result.setFqti(fqti);
            result.setType(URNType.DOMAIN);

        } else if (matched.equals("node")) {
            fqti += ":domain="+domainId+":node="+nodeId;
            result.setDomainId(domainId);
            result.setNodeId(nodeId);
            result.setFqti(fqti);
            result.setType(URNType.NODE);

        } else if (matched.equals("port")) {
            fqti += ":domain="+domainId+":node="+nodeId+":port="+portId;
            result.setDomainId(domainId);
            result.setNodeId(nodeId);
            result.setPortId(portId);
            result.setFqti(fqti);
            result.setType(URNType.PORT);

        } else if (matched.equals("link")) {
            fqti += ":domain="+domainId+":node="+nodeId+":port="+portId+":link="+linkId;
            concise = "::";
            if (!domainId.equals(localDomainId)) {
                concise += domainId+":";
            } else {
                concise += ":";
            }
            concise += nodeId+":"+portId+":"+linkId;

            result.setDomainId(domainId);
            result.setNodeId(nodeId);
            result.setPortId(portId);
            result.setLinkId(linkId);
            result.setFqti(fqti);
            result.setConcise(concise);

            result.setType(URNType.LINK);
        } else {
            result.setType(URNType.UNKNOWN);
            return result;
        }

        return result;
    }

    /**
     * Returns an abbreviated version of the full layer 2 topology identifier.
     *
     * @param topoIdent string with full topology identifier, for example
     * urn:ogf:network:domain=es.net:node=bnl-mr1:port=TenGigabitEthernet1/3:link=*
     * @return string abbreviation such as es.net:bnl-mr1:TenGigabitEthernet1/3
     */
    public static String abbreviate(String topoIdent) {
        Pattern p = Pattern.compile(
            "^urn:ogf:network:domain=([^:]+):node=([^:]+):port=([^:]+):link=([^:]+)$");
        Matcher matcher = p.matcher(topoIdent);
        String domainId = "";
        String nodeId = "";
        String portId = "";
        // punt if not in expected format
        if (!matcher.matches()) {
            return topoIdent;
        } else {
            domainId = matcher.group(1);
            nodeId = matcher.group(2);
            portId = matcher.group(3);
            return domainId + ":" + nodeId + ":" + portId;
        }
    }
}
