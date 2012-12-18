package net.es.oscars.wbui.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import org.apache.log4j.*;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.authN.soap.gen.policy.*;
import net.es.oscars.utils.config.*;
import net.es.oscars.utils.soap.OSCARSServiceException;

/**
 * Holds the names of the user and session cookies and whether they are secure cookies
 * and the guestLogin string
 * 
 * Gets and sets the cookies in the httpServletRequest and response.
 * Checks with the AuthNPolicy server if the session is valid and returns
 * the user's attributes.
 * 
 * @author davidR, mrt
 *
 */
public class UserSession {

    private String userCookieName;
    private String sessionCookieName;
    private boolean secureCookie;
    private String guestLogin;
    private Logger log = Logger.getLogger(UserSession.class);

    /**
     * constructor
     *    populates its fields from the servlet core for this user
     * @param core
     */
    public UserSession(ServletCore core) {
        userCookieName = core.getUserCookieName();
        sessionCookieName = core.getSessionCookieName();;
        guestLogin = core.getGuestLogin();
        String secureCookieProp = core.getSecureCookie();
        secureCookie = secureCookieProp.equals("1") ? true : false;
    }

    /**
     * Gets the user and session cookies from the http request and uses them
     * to check that the session is valid.
     * 
     * @param out PrinterWriter used for error handling
     * @param client  AuthNPolicy client
     * @param request the httpServletRequest
     * @param methodName The method the user is calling. Used in error handling
     * @return CheckSessionReply -contains the users Login name (now redundant) and all
     *          the user's attributes including loginName and institution.
     */
    public CheckSessionReply
        checkSession(PrintWriter out, AuthNPolicyClient client,
                     HttpServletRequest request, String methodName) {
        String userName = this.getCookie(this.userCookieName, request);
        String sessionName = this.getCookie(this.sessionCookieName, request);
        String errorMsg;

        if ((userName == null) || (sessionName == null)) {
            String status = "Your login session has expired. ";
            if ((userName == null) && (sessionName == null)) {
                status += "Login cookies are not set. ";
            } else if (userName == null) {
                status += "The user name cookie is not set. ";
            } else if (sessionName == null) {
                status += "The session name cookie is not set. ";
            }
            status += "Please try logging in again.";
            ServletUtils.handleFailure(out, status, methodName);
            return null;
        }
        AttrReply attrReply = null;
        try {
            SessionOpParams validSessionReq = new SessionOpParams();
            validSessionReq.setUserName(userName);
            validSessionReq.setSessionName(sessionName); 
            Object[] req = new Object[]{validSessionReq};
            Object[] resp = client.invoke("validSession", req);
            attrReply = (AttrReply) resp[0];
        } catch (OSCARSServiceException e) {
            ServletUtils.handleFailure(out, log, e, methodName);
            return null;
        }
        CheckSessionReply reply = new CheckSessionReply();
        reply.setUserName(userName);
        List<AttributeType> attributes = new ArrayList<AttributeType>();
        List<AttributeType> replyAttributes =
            attrReply.getSubjectAttributes();
        for (AttributeType attr: replyAttributes) {
            attributes.add(attr);
        }
        reply.setAttributes(attributes);
        return reply;
    }

    /**
     * Sets a cookie in the httpServletResponse
     * 
     * @param cookieName user or session cookie
     * @param cookieValue
     * @param response the serveltRespose in which the cookie is set
     */
    public void setCookie(String cookieName, String cookieValue,
            HttpServletResponse response) {

        Cookie cookie = this.handleCookie(cookieName, cookieValue);
        cookie.setMaxAge(60*60*8); // expire after 8 hours
        response.addCookie(cookie);
    }
    
    /**
     * sets an expired cookie in the servletResponse 
     * @param cookieName user or session cookie
     * @param cookieValue
     * @param response the serveltRespose in which the cookie is set
     */
    public void expireCookie(String cookieName, String cookieValue,
            HttpServletResponse response) {

        Cookie cookie = this.handleCookie(cookieName, cookieValue);
        cookie.setMaxAge(0); // remove cookie
        response.addCookie(cookie);
    }

    /**
     * gets the specified cookie from the httpServletResponse
     * 
     * @param cookieName user or session cookie
     * @param request the serveltRequest containing the cookie
     * @return the value of the cookie
     */
    public String getCookie(String cookieName, HttpServletRequest request) {

        String receivedCookieName = null;

        if (cookieName.equals("userName")) {
            receivedCookieName = this.userCookieName;
        } else if (cookieName.equals("sessionName")) {
            receivedCookieName = this.sessionCookieName;
        } else {
            receivedCookieName = cookieName;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) { return null; }
        for (int i=0; i < cookies.length; i++) {
            Cookie c = cookies[i];
            if ((c.getName().equals(receivedCookieName)) &&
                    (c.getValue() != null)) {
                return c.getValue();
            }
        }
        return null;
    }

    public String getGuestLogin() {
        return this.guestLogin;
    }

    private Cookie handleCookie(String cookieName, String cookieValue) {

        String sentCookieName = null;

        // special cases to handle less obvious cookie names being used
        if (cookieName.equals("userName")) {
            sentCookieName = this.userCookieName;
        } else if (cookieName.equals("sessionName")) {
            sentCookieName = this.sessionCookieName;
        } else {
            sentCookieName = cookieName;
        }
        Cookie cookie = new Cookie(sentCookieName, cookieValue);
        cookie.setVersion(1);
        // whether has to go over SSL
        cookie.setSecure(this.secureCookie);
        return cookie;
    }
}
