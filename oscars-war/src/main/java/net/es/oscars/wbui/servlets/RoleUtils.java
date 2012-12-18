package net.es.oscars.wbui.servlets;

import java.io.PrintWriter;
import javax.servlet.http.*;
import java.util.*;

import org.apache.log4j.Logger;
import net.sf.json.*;

import net.es.oscars.authCommonPolicy.soap.gen.AttrDetails;

public class RoleUtils {

    /* Get the role names from a request and translate them to
     *   a list of AttributeIds
     *
     *   @param request
     */
    public ArrayList<String> checkRoles(String roles[],
                                        List<AttrDetails> attributes) {

        ArrayList<String> addRoles = new ArrayList<String>();
        Logger log = Logger.getLogger(this.getClass());
        if (roles != null && roles.length > 0) {
            String st;
            for (String s : roles) {
                log.debug("role is " + s);
                if (s != null && !s.trim().equals("")) {
                    st = s.trim();
                    for (AttrDetails attr : attributes) {
                        if (attr.getValue().equals(st)) {
                            addRoles.add(attr.getValue());
                        }
                    }
                }
            }
        }
        return addRoles;
    }
}
