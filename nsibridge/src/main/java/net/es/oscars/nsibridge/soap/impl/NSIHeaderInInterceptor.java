package net.es.oscars.nsibridge.soap.impl;

import org.apache.cxf.binding.soap.interceptor.SoapHeaderInterceptor;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NSIHeaderInInterceptor extends SoapHeaderInterceptor {

    protected Logger log = Logger.getLogger(getClass());


    @Override public void handleMessage(Message message) throws Fault {


    }

}