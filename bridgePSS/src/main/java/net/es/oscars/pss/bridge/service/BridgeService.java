package net.es.oscars.pss.bridge.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.OSCARSFaultReport;
import net.es.oscars.pss.api.CircuitService;
import net.es.oscars.pss.api.Connector;
import net.es.oscars.pss.api.DeviceConfigGenerator;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSCommand;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.CircuitServiceConfig;
import net.es.oscars.pss.bridge.util.BridgeUtils;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.enums.ActionType;
import net.es.oscars.pss.util.ActionUtils;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.pss.util.ConnectorUtils;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.topology.PathTools;

public class BridgeService implements CircuitService {
    private Logger log = Logger.getLogger(BridgeService.class);
    public static final String SVC_ID = "bridge";

    /**
     * Always fails (for now)
     * @throws PSSException 
     */
    public List<PSSAction> modify(List<PSSAction> actions) throws PSSException {
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

        for (PSSAction action : actions) {
            ResDetails res = action.getRequest().getSetupReq().getReservation();
            List<String> deviceIds = BridgeUtils.getDeviceIds(res);
            
            for (String deviceId : deviceIds) {
                action = this.processActionForDevice(action, deviceId);
            }
            
            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
            
        }
        return results;
    }
    
    public List<PSSAction> teardown(List<PSSAction> actions) throws PSSException {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();

        for (PSSAction action : actions) {
            ResDetails res = action.getRequest().getTeardownReq().getReservation();
            
            List<String> deviceIds = BridgeUtils.getDeviceIds(res);
            
            for (String deviceId : deviceIds) {
                action = this.processActionForDevice(action, deviceId);
            }
            
            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
            
        }
        return results;
    }
    
    // TODO: implement status checking
    public List<PSSAction> status(List<PSSAction> actions) {
        ArrayList<PSSAction> results = new ArrayList<PSSAction>();
        for (PSSAction action : actions) {
            action.setStatus(ActionStatus.SUCCESS);
            results.add(action);
            ClassFactory.getInstance().getWorkflow().update(action);
        }
        return results;
    }
    
    
    
    private PSSAction processActionForDevice(PSSAction action, String deviceId) throws PSSException {
        String errorMessage = null;
        OSCARSFaultReport faultReport = new OSCARSFaultReport ();
        faultReport.setDomainId(PathTools.getLocalDomainId());
        ResDetails res = null;

        try {
            res = ActionUtils.getReservation(action);
        } catch (PSSException e) {
            log.error(e);
            e.printStackTrace();

            errorMessage = "Could not locate ResDetails for device "+deviceId+"\n"+e.getMessage();
            action.setStatus(ActionStatus.FAIL);
            faultReport.setErrorMsg(errorMessage);
            faultReport.setErrorType(ErrorReport.SYSTEM);
            faultReport.setErrorCode(ErrorCodes.CONFIG_ERROR);
            action.setFaultReport(faultReport);
            ClassFactory.getInstance().getWorkflow().update(action);
            throw new PSSException(e);
        }
        
        DeviceConfigGenerator cg = null;
        try {
            cg = ConnectorUtils.getDeviceConfigGenerator(deviceId, SVC_ID);
        } catch (PSSException e) {
            log.error(e);
            e.printStackTrace();
            errorMessage = "Could not locate config generator for device "+deviceId+"\n"+e.getMessage();
            action.setStatus(ActionStatus.FAIL);
            faultReport.setErrorMsg(errorMessage);
            faultReport.setGri(res.getGlobalReservationId());
            faultReport.setErrorType(ErrorReport.SYSTEM);
            faultReport.setErrorCode(ErrorCodes.CONFIG_ERROR);
            action.setFaultReport(faultReport);
            ClassFactory.getInstance().getWorkflow().update(action);
            throw new PSSException(e);
        }

        String deviceCommand = cg.getConfig(action, deviceId);
        String deviceAddress = ConnectorUtils.getDeviceAddress(deviceId);

        
        Connector conn = ClassFactory.getInstance().getDeviceConnectorMap().getDeviceConnector(deviceId);
        log.debug("connector for "+deviceId+" is: "+conn.getClass());
        
        if (ConfigHolder.getInstance().getBaseConfig().getCircuitService().isStub()) {
            log.debug("stub mode! connector will not send commands");
        } 
        
        PSSCommand comm = new PSSCommand();
        comm.setDeviceCommand(deviceCommand);
        comm.setDeviceAddress(deviceAddress);
        try {
            conn.sendCommand(comm);
        } catch (PSSException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            action.setStatus(ActionStatus.FAIL);
            faultReport.setErrorMsg(errorMessage);
            faultReport.setGri(res.getGlobalReservationId());
            faultReport.setErrorType(ErrorReport.SYSTEM);
            if (action.getActionType().equals(ActionType.MODIFY)) {
                faultReport.setErrorCode(ErrorCodes.UNKNOWN);
            } else if (action.getActionType().equals(ActionType.SETUP)) {
                faultReport.setErrorCode(ErrorCodes.PATH_SETUP_FAILED);
            } else if (action.getActionType().equals(ActionType.STATUS)) {
                faultReport.setErrorCode(ErrorCodes.UNKNOWN);
            } else if (action.getActionType().equals(ActionType.TEARDOWN)) {
                faultReport.setErrorCode(ErrorCodes.PATH_TEARDOWN_FAILED);
                
            }
            action.setFaultReport(faultReport);
            
            ClassFactory.getInstance().getWorkflow().update(action);
            throw e;
        }
        System.out.println("sent command!");
        return action;
    }
    
    public void setConfig(CircuitServiceConfig config) {
    }

}
