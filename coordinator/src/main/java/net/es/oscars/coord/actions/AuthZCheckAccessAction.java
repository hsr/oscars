package net.es.oscars.coord.actions;

import java.util.List;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.log4j.Logger;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.coord.actions.CoordAction;
import net.es.oscars.coord.common.CoordImpl;

import net.es.oscars.coord.workers.AuthZWorker;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.soap.OSCARSServiceException;

import net.es.oscars.authZ.soap.gen.CheckAccessParams;
import net.es.oscars.authZ.soap.gen.CheckAccessReply;

import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.sharedConstants.StateEngineValues;

/**
 * AuthZCheckAccessAction
 * Calls the AuthZ service via  AuthZwoker to check the permission of the
 * subjectAttributes,resource and permission that a request has stored in its
 * requestData.
 * 
 * @author lomax
 * @param input CheckAccessParams[subjectAttributes,resource,permission] is placed in the RequestData field
 * @return output CheckAccessRply[permission,AuthConditions] is placed in the ResultData field
 *
 */
public class AuthZCheckAccessAction extends CoordAction <CheckAccessParams, CheckAccessReply> {

    private static final long       serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(AuthZCheckAccessAction.class.getName());
    private static final String moduleName = ModuleName.COORD;
    
    @SuppressWarnings("unchecked")
    public AuthZCheckAccessAction (String name, CoordRequest request) {
        super (name, request, null);
    }
    
  /**
    * Send a checkAccess message to the AuthZ service
    * called synchronously
    *
    * @param are set in the constructor:
    */
    public void execute() {
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "AuthZCheckAccess";
        LOG.debug(netLogger.start(event));
        // Send a query to AuthZ to check user credentials. 
        try {
            AuthZWorker authZWorker = AuthZWorker.getInstance();
            CheckAccessParams checkAccessParams = this.getRequestData();
            
            Object[] req = new Object[]{checkAccessParams};
            Object[] res = authZWorker.getAuthZClient().invoke("checkAccess",req);

            if ((res == null) || (res[0] == null)) {
                throw new OSCARSServiceException ("AuthZCheckAccessAction:No response from AuthZ module",
                        "system");
            }
            CheckAccessReply authDecision = (CheckAccessReply) res[0];
            if (authDecision == null) {
                throw new OSCARSServiceException ("AuthZCheckAccessAction:Returned AuthDecision is null",
                        "system");
            }
            // Set the answers.
            this.setResultData(authDecision);
            // checkAccess is synchronous. Call executed.
            this.executed();
            String permission = authDecision.getPermission();
            String loginName = null;
            List<AttributeType> reqAttrs = checkAccessParams.getSubjectAttrs().getSubjectAttribute();
            for (AttributeType at : reqAttrs) {
                if (at.getName().equals(AuthZConstants.LOGIN_ID)) {
                    List<Object> values = at.getAttributeValue();
                    // Login should be only one value.
                    loginName= (String) values.get(0);
                }
            }
            LOG.debug(netLogger.end(event, "permission to " + checkAccessParams.getPermissionName() +
                                    " " + checkAccessParams.getResourceName() + " for " + loginName + " is " + permission));
        } catch (OSCARSServiceException ex) {
            this.fail(ex);
        } catch (Exception e)  {
            LOG.error (netLogger.error(event, ErrSev.MINOR,"caught Exception " + e.toString()));
            //e.printStackTrace();
            this.fail(e);
        }
    } 
}
