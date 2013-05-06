package net.es.oscars.pss.openflowj.config;

import java.util.ArrayList;
import java.util.List;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.NMWGParserUtil;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.openflow.protocol.OFMessage;

/**
 * Debug class only. Generates empty OpenFlow message set and prints parameters.
 * No messages will be sent tod evices with this class.
 *
 */
public class StubImplicitOFConfigGen extends ImplicitOFConfigGen{

    @Override
    public List<OFMessage> addFlow(CtrlPlaneHopContent inHop,
            CtrlPlaneHopContent outHop, ResDetails reservation)
                    throws PSSException {

        System.out.println("Implicit Setup:");
        try {
            System.out.println("    In Port: " + NMWGParserUtil.getURN(inHop, NMWGParserUtil.PORT_TYPE));
            System.out.println("    Out Port: " + NMWGParserUtil.getURN(outHop, NMWGParserUtil.PORT_TYPE));
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
        }

        return new ArrayList<OFMessage>();
    }

    @Override
    public List<OFMessage> removeFlow(CtrlPlaneHopContent inHop,
            CtrlPlaneHopContent outHop, ResDetails reservation)
                    throws PSSException {
        System.out.println("Implicit Teardown:");
        try {
            System.out.println("    In Port: " + NMWGParserUtil.getURN(inHop, NMWGParserUtil.PORT_TYPE));
            System.out.println("    Out Port: " + NMWGParserUtil.getURN(outHop, NMWGParserUtil.PORT_TYPE));
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
        }
        return new ArrayList<OFMessage>();
    }

}
