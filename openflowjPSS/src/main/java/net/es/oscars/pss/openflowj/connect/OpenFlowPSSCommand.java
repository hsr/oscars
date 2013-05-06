package net.es.oscars.pss.openflowj.connect;

import org.openflow.protocol.OFMessage;

import net.es.oscars.pss.beans.PSSCommand;

/**
 * Class that extends PSSCommand which is very string-centric, 
 * to allow for an OpenFlowMessage field to be passed around. 
 * A somewhat hacky way to get things to fit into the PSS framework.
 *
 */
public class OpenFlowPSSCommand extends PSSCommand {

    protected OFMessage openFlowMessage;

    public OFMessage getOpenFlowMessage() {
        return openFlowMessage;
    }

    public void setOpenFlowMessage(OFMessage openFlowMessage) {
        this.openFlowMessage = openFlowMessage;
    }


}
