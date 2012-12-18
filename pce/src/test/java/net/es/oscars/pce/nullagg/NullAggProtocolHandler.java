package net.es.oscars.pce.nullagg;

import java.util.List;
import java.util.ArrayList;
import org.apache.log4j.Logger;

import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.sharedConstants.PCERequestTypes;
import net.es.oscars.pce.AggMessage;
import net.es.oscars.pce.PCEProtocolHandler;
import net.es.oscars.pce.soap.gen.v06.AggregatorCreateContent;
import net.es.oscars.pce.soap.gen.v06.AggregatorModifyContent;
import net.es.oscars.pce.soap.gen.v06.AggregatorCreateCommitContent;
import net.es.oscars.pce.soap.gen.v06.AggregatorModifyCommitContent;
import net.es.oscars.pce.soap.gen.v06.AggregatorCancelContent;
import net.es.oscars.pce.soap.gen.v06.PCECancelContent;
import net.es.oscars.pce.soap.gen.v06.PCECreateCommitContent;
import net.es.oscars.pce.soap.gen.v06.PCEModifyCommitContent;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.pce.soap.gen.v06.TagDataContent;

@javax.xml.ws.BindingType(value ="http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class NullAggProtocolHandler extends PCEProtocolHandler {

    private static final Logger LOG = Logger.getLogger(PCEProtocolHandler.class.getName());
    private static String moduleName= "nullAggProtocolHandler";

    /* (non-Javadoc)
     * @see net.es.oscars.pce.soap.gen.v06.PCEPortType#aggregatorCreate(java.lang.String  globalReservationId ,)java.lang.String  pceName ,)java.lang.String  callBackEndpoint ,)java.util.List<net.es.oscars.pce.soap.gen.v06.TagDataContent>  pceData )*
     */
    public void aggregatorCreate(AggregatorCreateContent aggCreate) { 
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = aggCreate.getMessageProperties();
        String gri = aggCreate.getGlobalReservationId();
        netLogger.init(moduleName,msgProps.getGlobalTransactionId());
        netLogger.setGRI(gri);
        String pceName = aggCreate.getPceName();
        String callBackEndpoint = aggCreate.getCallBackEndpoint();
        List<TagDataContent> pceData = aggCreate.getPceData();
        try {
            LOG.info (netLogger.start(PCERequestTypes.AGGREGATOR_CREATE," pceName= " + pceName + " callback URL= " + callBackEndpoint));
            // Create a query object
            AggMessage pceQuery = new AggMessage (msgProps,
                                                  gri,
                                                  aggCreate.getId(),
                                                  pceName,
                                                  callBackEndpoint,
                                                  PCERequestTypes.PCE_CREATE,
                                                  pceData);
            // Add the query to the list
            NullAgg.getInstance().addPceQuery(pceQuery);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void pceCreateCommit(PCECreateCommitContent pceCreateCommit) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = pceCreateCommit.getMessageProperties();
        String event = PCERequestTypes.PCE_CREATE_COMMIT;
        netLogger.init(moduleName,msgProps.getGlobalTransactionId());
        String gri = pceCreateCommit.getGlobalReservationId();
        netLogger.setGRI(gri);
        String pceName = pceCreateCommit.getPceName();
        String callBackEndpoint = pceCreateCommit.getCallBackEndpoint();
        PCEDataContent pceData = pceCreateCommit.getPceData();
        try {
            LOG.info (netLogger.start(event, " pceName= " + pceName + " callback URL= " + callBackEndpoint));
            // Create a query object
            List<TagDataContent> pceTagData = new ArrayList <TagDataContent>();
            TagDataContent tagData = new TagDataContent ();
            tagData.setConstraints(pceData);
            tagData.setTag("NoTag"); // there is no need for a tag for the commit phase.
            pceTagData.add(tagData);
            AggMessage pceQuery = new AggMessage (msgProps,
                                                  gri,
                                                  pceCreateCommit.getId(),
                                                  pceName,
                                                  callBackEndpoint,
                                                  PCERequestTypes.PCE_CREATE_COMMIT,
                                                  pceTagData);

            // Add the query to the list
            NullAgg.getInstance().addPceQuery(pceQuery);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void pceModifyCommit(PCEModifyCommitContent pceModifyCommit) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = pceModifyCommit.getMessageProperties();
        String event = PCERequestTypes.PCE_MODIFY_COMMIT;
        netLogger.init(moduleName,msgProps.getGlobalTransactionId());
        String gri = pceModifyCommit.getGlobalReservationId();
        netLogger.setGRI(gri);
        String pceName = pceModifyCommit.getPceName();
        String callBackEndpoint = pceModifyCommit.getCallBackEndpoint();
        PCEDataContent pceData = pceModifyCommit.getPceData();
        try {
            LOG.info (netLogger.start(event, " pceName= " + pceName + " callback URL= " + callBackEndpoint));
            // Create a query object
            List<TagDataContent> pceTagData = new ArrayList <TagDataContent>();
            TagDataContent tagData = new TagDataContent ();
            tagData.setConstraints(pceData);
            tagData.setTag("NoTag"); // there is no need for a tag for the commit phase.
            pceTagData.add(tagData);
            AggMessage pceQuery = new AggMessage (msgProps,
                                                  gri,
                                                  pceModifyCommit.getId(),
                                                  pceName,
                                                  callBackEndpoint,
                                                  PCERequestTypes.PCE_MODIFY_COMMIT,
                                                  pceTagData);

            // Add the query to the list
            NullAgg.getInstance().addPceQuery(pceQuery);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    public void pceCancel(PCECancelContent pceCancel) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = pceCancel.getMessageProperties();
        String event = PCERequestTypes.PCE_CANCEL;
        netLogger.init(moduleName,msgProps.getGlobalTransactionId());
        String gri = pceCancel.getGlobalReservationId();
        netLogger.setGRI(gri);
        String pceName = pceCancel.getPceName();
        String callBackEndpoint = pceCancel.getCallBackEndpoint();
        PCEDataContent pceData = pceCancel.getPceData();
        try {
            LOG.info (netLogger.start(event, " pceName= " + pceName + " callback URL= " + callBackEndpoint));
            // Create a query object
            List<TagDataContent> pceTagData = new ArrayList <TagDataContent>();
            TagDataContent tagData = new TagDataContent ();
            tagData.setConstraints(pceData);
            tagData.setTag("NoTag"); // there is no need for a tag for cancel.
            pceTagData.add(tagData);
            AggMessage pceQuery = new AggMessage (msgProps,
                                                  gri,
                                                  pceCancel.getId(),
                                                  pceName,
                                                  callBackEndpoint,
                                                  PCERequestTypes.PCE_CANCEL,
                                                  pceTagData);
//          Add the query to the list
            NullAgg.getInstance().addPceQuery(pceQuery);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }                   
    }
    public void aggregatorModify(AggregatorModifyContent aggModify) { 
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = aggModify.getMessageProperties();
        String event = PCERequestTypes.AGGREGATOR_MODIFY;
        netLogger.init(moduleName,msgProps.getGlobalTransactionId());
        String gri = aggModify.getGlobalReservationId();
        netLogger.setGRI(gri);
        String pceName = aggModify.getPceName();
        String callBackEndpoint = aggModify.getCallBackEndpoint();
        List<TagDataContent> pceData = aggModify.getPceData();
        try {
            LOG.info (netLogger.start(event, " pceName= " + pceName + " callback URL= " + callBackEndpoint));
//          Create a query object
            AggMessage pceQuery = new AggMessage (msgProps,
                                                  gri,
                                                  aggModify.getId(),
                                                  pceName,
                                                  callBackEndpoint,
                                                  PCERequestTypes.PCE_MODIFY,
                                                  pceData);
//          Add the query to the list
            NullAgg.getInstance().addPceQuery(pceQuery);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }                   
    }
    public void aggregatorCreateCommit(AggregatorCreateCommitContent aggCreateCommit) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = aggCreateCommit.getMessageProperties();
        String event = PCERequestTypes.AGGREGATOR_CREATE_COMMIT;
        netLogger.init(moduleName,msgProps.getGlobalTransactionId());
        String gri = aggCreateCommit.getGlobalReservationId();
        netLogger.setGRI(gri);
        String pceName = aggCreateCommit.getPceName();
        String callBackEndpoint = aggCreateCommit.getCallBackEndpoint();
        List<TagDataContent> pceData = aggCreateCommit.getPceData();
        try {
            LOG.info (netLogger.start(event, " pceName= " + pceName + " callback URL= " + callBackEndpoint));
//          Create a query object
            AggMessage pceQuery = new AggMessage (msgProps,
                                                  gri,
                                                  aggCreateCommit.getId(),
                                                  pceName,
                                                  callBackEndpoint,
                                                  PCERequestTypes.PCE_CREATE_COMMIT,
                                                  pceData);
//          Add the query to the list
            NullAgg.getInstance().addPceQuery(pceQuery);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }                   
    }

    public void aggregatorModifyCommit(AggregatorModifyCommitContent aggModifyCommit) {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = aggModifyCommit.getMessageProperties();
        String event = PCERequestTypes.AGGREGATOR_MODIFY_COMMIT;
        netLogger.init(moduleName,msgProps.getGlobalTransactionId());
        String gri = aggModifyCommit.getGlobalReservationId();
        netLogger.setGRI(gri);
        String pceName = aggModifyCommit.getPceName();
        String callBackEndpoint = aggModifyCommit.getCallBackEndpoint();
        List<TagDataContent> pceData = aggModifyCommit.getPceData();
        try {
            LOG.info (netLogger.start(event, " pceName= " + pceName + " callback URL= " + callBackEndpoint));
//          Create a query object
            AggMessage pceQuery = new AggMessage (msgProps,
                                                  gri,
                                                  aggModifyCommit.getId(),
                                                  pceName,
                                                  callBackEndpoint,
                                                  PCERequestTypes.PCE_MODIFY_COMMIT,
                                                  pceData);
//          Add the query to the list
            NullAgg.getInstance().addPceQuery(pceQuery);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    public void aggregatorCancel(AggregatorCancelContent aggCancel) { 
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        MessagePropertiesType msgProps = aggCancel.getMessageProperties();
        String event = PCERequestTypes.AGGREGATOR_CANCEL;
        netLogger.init(moduleName,msgProps.getGlobalTransactionId());
        String gri = aggCancel.getGlobalReservationId();
        netLogger.setGRI(gri);
        String pceName = aggCancel.getPceName();
        String callBackEndpoint = aggCancel.getCallBackEndpoint();
        List<TagDataContent> pceData = aggCancel.getPceData();
        try {
            LOG.info (netLogger.start(event, " pceName= " + pceName + " callback URL= " + callBackEndpoint));
//          Create a query object
            AggMessage pceQuery = new AggMessage (msgProps,
                                                  gri,
                                                  aggCancel.getId(),
                                                  pceName,
                                                  callBackEndpoint,
                                                  PCERequestTypes.PCE_CANCEL,
                                                  pceData);
//          Add the query to the list
            NullAgg.getInstance().addPceQuery(pceQuery);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }                   
    }
}
