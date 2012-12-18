package net.es.oscars.pss.eompls.alu;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.eompls.dao.SRLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SR_VPLS_DeviceIdentifiers {
    private static Logger log = Logger.getLogger(SR_VPLS_DeviceIdentifiers.class);
    protected List<Integer> sdpIds;
    protected Integer qosId;

    public List<Integer> getSdpIds() {
        return sdpIds;
    }

    public void setSdpIds(List<Integer> sdpIds) {
        this.sdpIds = sdpIds;
    }

    public Integer getQosId() {
        return qosId;
    }

    public void setQosId(Integer qosId) {
        this.qosId = qosId;
    }


    public static SR_VPLS_DeviceIdentifiers retrieve(String gri, String deviceId) {
        SR_VPLS_DeviceIdentifiers srids = new SR_VPLS_DeviceIdentifiers();
        String idString = "";
        String qosScope = deviceId + ":qos";
        String sdpScope = deviceId + ":sdp";
        boolean foundQos = false;
        boolean foundSdps = false;

        List<Integer> ids = SRLUtils.getExistingIdentifiers(qosScope, gri);
        if (ids == null || ids.size() == 0) {
            log.debug("no qos ids found");
        } else if (ids.size() > 0) {
            idString = StringUtils.join(ids, ", ");
            log.error("multiple qos ids found: ["+idString+"] , getting first one");
            srids.setQosId(ids.get(0));
            foundQos = true;
        } else {
            srids.setQosId(ids.get(0));
            foundQos = true;
        }

        ids = SRLUtils.getExistingIdentifiers(sdpScope, gri);
        if (ids == null || ids.size() == 0) {
            log.debug("no sdp ids found");
        } else if (ids.size() > 0) {
            idString = StringUtils.join(ids, ", ");
            log.error("multiple sdp ids found: ["+idString+"]");
            srids.setSdpIds(ids);
            foundSdps = true;
        } else {
            srids.setSdpIds(ids);
            foundSdps = true;
        }

        if (foundQos && foundSdps) {
            log.debug("found all of: QoS, SDP ids");
            return srids;
        } else if (!foundQos && !foundSdps) {
            log.debug("found none of: QoS, SDP ids");
            return null;
        } else {
            log.warn("found partial ids: QoS: ["+srids.getQosId()+"], SDP ["+StringUtils.join(srids.getSdpIds(), ", ")+"] ids");
            return null;
        }
    }


    public static SR_VPLS_DeviceIdentifiers reserve(String gri, String deviceId, Integer preferred, Integer sdpNum) throws PSSException {
        SR_VPLS_DeviceIdentifiers srids = new SR_VPLS_DeviceIdentifiers();
        if (sdpNum <= 0 || sdpNum > 24) {
            throw  new PSSException("invalid number of sdps ("+sdpNum+") allowed: 1..24");
        }
        String rangeExpr = "6000-6999";
        String qosScope = deviceId + ":qos";
        String sdpScope = deviceId + ":sdp";

        Integer qosId  = SRLUtils.getIdentifier(qosScope, gri, preferred, rangeExpr);
        List<Integer> sdpIds = new ArrayList<Integer>();
        for (int i = 0; i < sdpNum; i++) {
            Integer sdpId = SRLUtils.getIdentifier(sdpScope, gri, preferred, rangeExpr);
            sdpIds.add(sdpId);
        }
        srids.setQosId(qosId);
        srids.setSdpIds(sdpIds);
        return srids;


    }


    public static void release(String gri, String deviceId) {
        String qosScope = deviceId + ":qos";
        String sdpScope = deviceId + ":sdp";
        String idString;


        List<Integer> ids;


        ids = SRLUtils.releaseIdentifiers(qosScope, gri);
        idString = StringUtils.join(ids, ", ");
        log.debug("released qos id(s) :"+idString+" for gri: "+gri+" device: "+deviceId);

        ids = SRLUtils.releaseIdentifiers(sdpScope, gri);
        idString = StringUtils.join(ids, ", ");
        log.debug("released sdp id(s) :"+idString+" for gri: "+gri+" device: "+deviceId);


    }

}
