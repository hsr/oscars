package net.es.oscars.pss.openflow.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import net.es.oscars.pss.api.CircuitService;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSCommand;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.CircuitServiceConfig;
import net.es.oscars.pss.notify.CoordNotifier;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.pss.openflow.nox.JSONConnector;
import net.es.oscars.pss.openflow.nox.JSONConfigGen;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.topology.PathTools;


public class OpenFlowService implements CircuitService {
    private Logger log = Logger.getLogger(OpenFlowService.class);
    public static final String SVC_ID = "openflow";

    public void setConfig(CircuitServiceConfig config) {
    }

    /**
     * Not suppoted. return "FAIL"
     */
    public List<PSSAction> modify(List<PSSAction> actions) {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        for (PSSAction action : actions) {
            action.setStatus(ActionStatus.FAIL);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }
        return results;
    }
    
    public List<PSSAction> setup(List<PSSAction> actions) throws PSSException {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        CoordNotifier coordNotify = new CoordNotifier();
        OSCARSFaultReport faultReport = new OSCARSFaultReport ();
        faultReport.setDomainId(PathTools.getLocalDomainId());
        for (PSSAction action : actions) {
            JSONConnector conn = (JSONConnector)ClassFactory.getInstance().getConnectorDirectory().getConnector("openflow-nox");
            if (conn == null) {
                action.setStatus(ActionStatus.FAIL);
                faultReport.setErrorMsg("unable to load 'openflow-nox' connector");
                faultReport.setErrorType(ErrorReport.SYSTEM);
                faultReport.setErrorCode(ErrorCodes.CONFIG_ERROR);
                action.setFaultReport(faultReport);
                ClassFactory.getInstance().getWorkflow().update(action);
                coordNotify.process(action);
                throw new PSSException("unable to load 'openflow-nox' connector");
            }
            try {                
                JSONConfigGen cg = new JSONConfigGen();
                String cmdString = cg.getConfig(action);
                PSSCommand setupCmd = new PSSCommand();
                setupCmd.setDeviceCommand(cmdString);
                String response = conn.sendCommand(setupCmd);
                checkErrorMessage(response);
                action.setStatus(ActionStatus.SUCCESS);
            } catch (PSSException e) {
                action.setStatus(ActionStatus.FAIL);
                faultReport.setErrorMsg(e.getMessage());
                faultReport.setErrorType(ErrorReport.SYSTEM);
                faultReport.setErrorCode(ErrorCodes.CONFIG_ERROR);
                action.setFaultReport(faultReport);
                ClassFactory.getInstance().getWorkflow().update(action);
                coordNotify.process(action);
                throw e;
            }
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
            coordNotify.process(action);
        }
        return results;
    }
    
    public List<PSSAction> teardown(List<PSSAction> actions) throws PSSException {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        CoordNotifier coordNotify = new CoordNotifier();
        OSCARSFaultReport faultReport = new OSCARSFaultReport ();
        faultReport.setDomainId(PathTools.getLocalDomainId());
        for (PSSAction action : actions) {
            JSONConnector conn = (JSONConnector)ClassFactory.getInstance().getConnectorDirectory().getConnector("openflow-nox");
            if (conn == null) {
                action.setStatus(ActionStatus.FAIL);
                faultReport.setErrorMsg("unable to load 'openflow-nox' connector");
                faultReport.setErrorType(ErrorReport.SYSTEM);
                faultReport.setErrorCode(ErrorCodes.CONFIG_ERROR);
                action.setFaultReport(faultReport);
                ClassFactory.getInstance().getWorkflow().update(action);
                coordNotify.process(action);
                throw new PSSException("unable to load 'openflow-nox' connector");
            }
            try {
                JSONConfigGen cg = new JSONConfigGen();
                String cmdString = cg.getConfig(action);
                PSSCommand teardownCmd = new PSSCommand();
                teardownCmd.setDeviceCommand(cmdString);
                String response = conn.sendCommand(teardownCmd);
                checkErrorMessage(response);
                action.setStatus(ActionStatus.SUCCESS);
            } catch (PSSException e) {
                action.setStatus(ActionStatus.FAIL);
                faultReport.setErrorMsg(e.getMessage());
                faultReport.setErrorType(ErrorReport.SYSTEM);
                faultReport.setErrorCode(ErrorCodes.CONFIG_ERROR);
                action.setFaultReport(faultReport);
                ClassFactory.getInstance().getWorkflow().update(action);
                coordNotify.process(action);
                throw e;
            }
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
            coordNotify.process(action);
        }
        return results;
    }
    
    // TODO: implement status checking
    public List<PSSAction> status(List<PSSAction> actions) throws PSSException {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        CoordNotifier coordNotify = new CoordNotifier();
        OSCARSFaultReport faultReport = new OSCARSFaultReport ();
        faultReport.setDomainId(PathTools.getLocalDomainId());
        for (PSSAction action : actions) {
            JSONConnector conn = (JSONConnector)ClassFactory.getInstance().getConnectorDirectory().getConnector("openflow-nox");
            if (conn == null) {
                action.setStatus(ActionStatus.FAIL);
                faultReport.setErrorMsg("unable to load 'openflow-nox' connector");
                faultReport.setErrorType(ErrorReport.SYSTEM);
                faultReport.setErrorCode(ErrorCodes.CONFIG_ERROR);
                action.setFaultReport(faultReport);
                ClassFactory.getInstance().getWorkflow().update(action);
                coordNotify.process(action);
                throw new PSSException("unable to load 'openflow-nox' connector");
            }
            try {
                JSONConfigGen cg = new JSONConfigGen();
                String cmdString = cg.getConfig(action);
                PSSCommand statusCmd = new PSSCommand();
                statusCmd.setDeviceCommand(cmdString);
                String response = conn.sendCommand(statusCmd);
                checkErrorMessage(response);
                action.setStatus(ActionStatus.SUCCESS);
            } catch (PSSException e) {
                action.setStatus(ActionStatus.FAIL);
                faultReport.setErrorMsg(e.getMessage());
                faultReport.setErrorType(ErrorReport.SYSTEM);
                faultReport.setErrorCode(ErrorCodes.CONFIG_ERROR);
                action.setFaultReport(faultReport);
                ClassFactory.getInstance().getWorkflow().update(action);
                coordNotify.process(action);
                throw e;
            }
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
            coordNotify.process(action);
        }
        return results;
    }

    private void checkErrorMessage(String response) throws PSSException {
        response.replaceAll("\n", "");
        Pattern p = Pattern.compile("^\\s*\\{[^\\}]+\\}\\s*$");
        Matcher m = p.matcher(response);
        if (!m.matches() || !response.contains("oscars-reply"))
            throw new PSSException("malformed NoX JSON response message:\n"+response);
        p = Pattern.compile("^\\{\"status\"\\s*:\\s*\"([^\"]+)\".*$"); 
        m = p.matcher(response);
        if (!m.matches())
            throw new PSSException("malformed NoX JSON response message:\n"+response);
        else if(!m.group(1).equals("FAILED"))
            return;
        p = Pattern.compile("^.*\"err_msg\"\\s*:\\s*\"([^\"]+)\".*$"); 
        m = p.matcher(response);
        if (m.matches())
            throw new PSSException("NOX controller returns error: " + m.group(1));
    }
}
