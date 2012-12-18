package net.es.oscars.wbui.servlets;

import java.util.List;

import oasis.names.tc.saml._2_0.assertion.AttributeType;

/**
 * Bean used by UserSession.checkSession
 */
public class CheckSessionReply {

    private String userName;
    private List<AttributeType> attributes;

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<AttributeType> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(List<AttributeType> attributes) {
        this.attributes = attributes;
    }
}
