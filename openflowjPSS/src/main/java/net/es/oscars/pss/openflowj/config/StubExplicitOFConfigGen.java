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
 * Debugging class only. Returns an empty list of messages and prints 
 * some of the parameters of the request.Does not actually lead to any 
 * device configuration
 *
 */
public class StubExplicitOFConfigGen extends ExplicitOFConfigGen{

    @Override
    public List<OFMessage> addFlow(CtrlPlaneHopContent inHop,
            CtrlPlaneHopContent outHop, ResDetails reservation)
                    throws PSSException {

        System.out.println("Explicit Setup:");
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
        System.out.println("Explicit Teardown:");
        try {
            System.out.println("    In Port: " + NMWGParserUtil.getURN(inHop, NMWGParserUtil.PORT_TYPE));
            System.out.println("    Out Port: " + NMWGParserUtil.getURN(outHop, NMWGParserUtil.PORT_TYPE));
        } catch (OSCARSServiceException e) {
            e.printStackTrace();
        }
        return new ArrayList<OFMessage>();
    }
}
